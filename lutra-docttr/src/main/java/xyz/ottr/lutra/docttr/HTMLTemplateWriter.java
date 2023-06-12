package xyz.ottr.lutra.docttr;

/*-
 * #%L
 * lutra-docttr
 * %%
 * Copyright (C) 2018 - 2019 University of Oslo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import static j2html.TagCreator.*;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.Space;
import xyz.ottr.lutra.docttr.visualisation.DependencyGraphVisualiser;
import xyz.ottr.lutra.docttr.visualisation.TripleInstanceGraphVisualiser;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.Substitution;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.store.Expander;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.store.expansion.NonCheckingExpander;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;
import xyz.ottr.lutra.wottr.util.PrefixMappings;
import xyz.ottr.lutra.wottr.writer.WInstanceWriter;
import xyz.ottr.lutra.writer.RDFNodeWriter;

public class HTMLTemplateWriter {

    private final PrefixMapping prefixMapping;
    private final TemplateStore store;
    private final Expander expander;

    private final SerialisationWriter serialisationWriter;
    private final DependencyGraphVisualiser dependencyGraphVisualiser;

    private ContainerTag tocList;

    public HTMLTemplateWriter(PrefixMapping prefixMapping, TemplateStore store) {
        this.prefixMapping = prefixMapping;
        this.prefixMapping.withDefaultMappings(OTTR.getStandardLibraryPrefixes());
        this.prefixMapping.setNsPrefix("x", OTTR.ns_example_arg);

        this.store = store;
        expander = new NonCheckingExpander(store);

        this.serialisationWriter = new SerialisationWriter(this.prefixMapping);
        this.dependencyGraphVisualiser = new DependencyGraphVisualiser(this.prefixMapping);
    }

    public String write(String iri, Result<Signature> result) {
        return result
            .map(signature -> document(getHTML(signature)))
            .orElse(getErrorPage(iri, result));
    }

    private String getErrorPage(String iri, Result<Signature> result) {

        var messages = result.getAllMessages().stream()
            .map(Message::toString)
            .collect(joining(Space.LINEBR2));

        return document(
            html(
                HTMLFactory.getHead("Error: " + iri),
                body(
                    h1(join("Error: ", code(iri))),
                    p("Processing " + iri + " gave an error."),
                    pre(messages)
                )
            )
        );
    }

    private ContainerTag getHTML(Signature signature) {

        this.tocList = ul(li(a("top").withHref("#top")));

        var exampleInstance = signature.getExampleInstance();
        var expansionTree = getExpansionTree(exampleInstance);

        PrefixMapping usedPrefixes = PrefixMappings.trim(signature, this.prefixMapping);

        return html(
            getHead(signature),
            body(
                span().withId("top"),
                div(b("Contents"), tocList).withId("ToC"),
                h1(join(
                    signature.getClass().getSimpleName() + ": ",
                    code(RDFNodeWriter.toString(this.prefixMapping, signature.getIri())))),
                div(
                    div(
                        p(
                            text("URI: "),
                            code(a(signature.getIri()).withHref(signature.getIri()))
                        )
                    ),
                    writeAnnotations(signature),
                    writeParameters(signature),
                    writePattern(signature, expansionTree),
                    writeDependencies(signature),
                    signature instanceof Template ? writeMetrics(expansionTree) : null,
                    writeSerialisations(signature),

                    getTOCHeading("Prefixes"),
                    HTMLFactory.getPrefixDiv(usedPrefixes).withId("prefixes")
                ),
                HTMLFactory.getFooterDiv(),
                HTMLFactory.getScripts()
            )
        );
    }

    private ContainerTag getTOCHeading(String heading) {
        tocList.with(li(a(heading).withHref("#" + heading)));
        return h2(heading).withId(heading);
    }

    private ContainerTag writeAnnotations(Signature signature) {

        var annotations = signature.getAnnotations();

        WInstanceWriter instanceWriter = new WInstanceWriter(this.prefixMapping);

        ResultStream.innerOf(annotations)
            .innerFlatMap(this.expander::expandInstanceFetch)
            .innerForEach(instanceWriter);

        Model metaData = instanceWriter.writeToModel();
        metaData.setNsPrefixes(this.prefixMapping);

        var container = div().withId("metadata");

        if (!metaData.isEmpty()) {

            var modelLister = new ModelListRenderer(metaData);

            container.with(
                getTOCHeading("Metadata"),
                HTMLFactory.getInfoP("This section contains the data represented by the signature's annotation instances."),
                modelLister.drawList(signature.getIri()),
                details(
                    summary("Metadata as RDF graph"),
                    pre(this.serialisationWriter.writeRDF(metaData))
                ) //, ul(each(annotations, a -> li(a.toString(this.prefixMapping))))
            );
        }
        return container;
    }

    private ContainerTag writeParameters(Signature signature) {

        var table = table(
            tr(
                th("Index"),
                th("Name"),
                th("Type"),
                th("Optional"),
                th("Blanks allowed"),
                th("Default value")
            ));

        var parameters = signature.getParameters();

        for (int index = 0; index < parameters.size(); index += 1) {
            var p = parameters.get(index);
            table.with(tr(
                td(Integer.toString(index + 1)),
                td(text(Objects.requireNonNullElse(p.getName(),
                        this.prefixMapping.shortForm(p.getTerm().getIdentifier().toString())))),
                td(this.prefixMapping.shortForm(p.getTerm().getType().toString())),
                td(p.isOptional() ? "yes" : "no"),
                td(p.isNonBlank() ? "no" : "yes"),
                td(p.hasDefaultValue()
                    ? this.prefixMapping.shortForm(p.getDefaultValue().getIdentifier().toString())
                    : "no")
                )
            );
        }

        return div(
            getTOCHeading("Parameters"),
            HTMLFactory.getInfoP("The parameters defined by the signature are listed here. "
                + "Unfortunately parameter names are not informative; fixing it is a planned future feature. "
                + "An optional parameter will accept the value ottr:none as an argument. "
                + "A parameter which allows blanks will accept a blank node as argument value. "),
            table
        ).withId("parameters");
    }

    private ContainerTag writePattern(Signature signature, Tree<Instance> exampleInstanceTree) {

        if (!(signature instanceof Template)) {
            return div(
                getTOCHeading("Pattern"),
                p("The resource is a " + signature.getClass().getSimpleName() + " and has no pattern."));
        } else {
            var exampleInstance = exampleInstanceTree.getRoot();
            var exampleExpansion = this.getExpansion(exampleInstance);
            var wexampleInstance = this.serialisationWriter.writeWottrModel(exampleInstance);
            var expansionViz = new TripleInstanceGraphVisualiser(this.prefixMapping);
            this.expander.expandInstance(exampleInstance)
                .innerForEach(expansionViz);

            return div(
                getTOCHeading("Pattern"),
                h4("stOTTR"),
                HTMLFactory.getInfoP("stOTTR serialisation of the template without annotation instances."),
                pre(this.serialisationWriter.writeStottrPattern(signature)),
                HTMLFactory.getInfoP("The pattern of the template is illustrated by expanding a generated instance. "
                    + "Below the generated instance is shown in different serialisations,"
                    + " and its expansion is presented in different formats."),
                h4("Generated instance"),
                b("stOTTR").withClass("heading"),
                pre(this.serialisationWriter.writeStottr(exampleInstance)),
                b("RDF/wOTTR").withClass("heading"),
                pre(this.serialisationWriter.writeRDF(wexampleInstance)),
                h4("Visualisation of expanded RDF graph"),
                HTMLFactory.getInfoP("Each resource node is linked to its IRI. Type relationships are not visualised,"
                    + " rather each node contains its type."),
                expansionViz.draw(exampleInstance.getArguments()),
                h4("Expanded RDF graph"),
                pre(this.serialisationWriter.writeRDF(exampleExpansion)),
                h4("Interactive expansion"),
                HTMLFactory.getInfoP("Click the list to expand/contract one list element. "
                    + "Click 'expand/contact all' to expand/contract all elements. "
                    + "Note that the interactive expansion is not correct for instances that are marked by list expanders."),
                div(writeInteractiveExpansion(exampleInstanceTree))
            ).withId("pattern");
        }
    }

    private ContainerTag writeInteractiveExpansion(Tree<Instance> expansionTree) {

        // Convert expansion tree to html list
        var stOTTRInstanceTreeViewWriter = new TreeViewWriter<Instance>() {
            @Override
            protected ContainerTag writeRoot(Tree<Instance> root) {
                return code(HTMLTemplateWriter.this.serialisationWriter.writeStottr(root.getRoot()));
            }

            @Override
            protected Collection<Tree<Instance>> prepareChildren(List<Tree<Instance>> children) {
                children.sort(Comparator.comparing(a -> a.getRoot().getIri()));
                return children;
            }
        };

        return stOTTRInstanceTreeViewWriter.write(expansionTree);
    }

    private Tree<Instance> getExpansionTree(Instance exampleInstance) {
        // Build expansion tree
        Function<Instance, List<Instance>> builder = instance -> {
            var templateIRI = instance.getIri();
            var signature = this.store.getSignature(templateIRI).get();
            if (!(signature instanceof Template)) {
                return Collections.emptyList();
            } else {
                // TODO: create a substitution from a Map directly to avoid validation
                var substitution = Substitution.resultOf(instance.getArguments(), signature.getParameters()).get();
                return this.store.getTemplate(instance.getIri()).get().getPattern().stream()
                    .map(is -> is.apply(substitution))
                    .collect(Collectors.toList());
            }
        };
        return new Tree<>(exampleInstance, builder);
    }

    private DomContent writeDependencies(Signature signature) {

        var tree = getDependencyTree(signature);

        return div(
            getTOCHeading("Dependencies"),
            signature instanceof Template
                ? join(
                    h4("Dependency graph"),
                    HTMLFactory.getInfoP("The graph shows all the templates that this template depends on. "
                        + "The colour of the node indicates its namespace. "
                        + "Each node is linked to its documentation page."),
                    this.dependencyGraphVisualiser.drawTree(tree),
                    h4("List of dependencies"),
                    HTMLFactory.getInfoP("The number in parenthesis is the number of instances of each template."),
                    writeDependenciesList(tree)
                    )
                : p("The resource is a " + signature.getClass().getSimpleName()
                    + " and depends on no other templates."),
            h4("Depending templates"),
            HTMLFactory.getInfoP("The templates in this library that depend on this template."),
            writeDependingTemplates(signature)
        ).withId("dependencies");
    }

    private ContainerTag writeDependingTemplates(Signature signature) {
        var dependingTemplates = new TreeSet<String>();

        this.store.getAllTemplates()
            .innerFilter(
                template -> template.getPattern().stream()
                    .map(Instance::getIri)
                    .anyMatch(iri -> iri.equals(signature.getIri())))
            .innerForEach(template -> dependingTemplates.add(template.getIri()));

        if (dependingTemplates.isEmpty()) {
            return p("None found.");
        }

        return ul(each(dependingTemplates, iri -> li(getRelativeA(iri, signature.getIri()))));
    }

    private ContainerTag writeDependenciesList(Tree<String> dependencies) {

        var depTreeViewWriter = new TreeViewWriter<String>() {
            @Override
            protected ContainerTag writeRoot(Tree<String> root) {
                var iri = span(RDFNodeWriter.toString(HTMLTemplateWriter.this.prefixMapping, root.getRoot()));
                if (!root.isRoot()) {
                    var count = root.getParent().getChildren().stream()
                        .filter(child -> child.getRoot().equals(root.getRoot()))
                        .count();
                    iri.with(span(" (" + count + ")"));
                }
                return iri;
            }

            @Override
            protected Collection<Tree<String>> prepareChildren(List<Tree<String>> children) {
                // group by iri so we can pick only one.
                var map = children.stream()
                    .collect(groupingBy(Tree::getRoot));

                return map.keySet().stream()
                    .map(iri -> map.get(iri).get(0))
                    .sorted(Comparator.comparing(Tree::getRoot))
                    .collect(Collectors.toList());
            }
        };

        return depTreeViewWriter.write(dependencies);
    }

    private Tree<String> getDependencyTree(Signature signature) {
        Function<String, List<String>> builder = iri ->
            this.store.getTemplate(iri)
                .map(t -> t.getPattern().stream()
                    .map(Instance::getIri)
                    .sorted().collect(Collectors.toList()))
                .orElse(Collections.emptyList());

        return new Tree<>(signature.getIri(), builder);
    }

    private ContainerTag writeMetrics(Tree<Instance> exampleInstanceTree) {

        var direct = exampleInstanceTree.getChildren().stream()
            .map(Tree::getRoot)
            .collect(Collectors.toList());

        var transitive = exampleInstanceTree.preorderStream()
            .map(Tree::getRoot)
            .collect(Collectors.toList());

        return div(
            getTOCHeading("Metrics"),
            HTMLFactory.getInfoP("Dependency graph metrics. "
                + "Depth is the number of steps to a leaf node in the dependency graph. "
                + "Branching is the number of outgoing edges from a node."),
            ul(
                li("Max. dependency depth: " + exampleInstanceTree.getMaxDepth()),
                li("Min. dependency depth: " + exampleInstanceTree.getMinDepth()),
                li("Max. branching: " + exampleInstanceTree.getMaxChildren()),
                li("Min. branching: " + exampleInstanceTree.getMinChildren())
            ),
            h4("Templates used"),
            table(tr(
                td(b("Direct dependencies").withClass("heading"),
                    getInstanceMetricsList(exampleInstanceTree.getRoot().getIri(), direct)
                ),
                td(
                    b("Complete expansion").withClass("heading"),
                    getInstanceMetricsList(exampleInstanceTree.getRoot().getIri(), transitive)
                ))),
            h4("Vocabulary introduced"),
            table(tr(
                td(b("Direct dependencies").withClass("heading"),
                    getVocabularyMetricsList(direct)
                ),
                td(
                    b("Complete expansion").withClass("heading"),
                    getVocabularyMetricsList(transitive)
                )))
        ).withId("metrics");
    }

    private ContainerTag getInstanceMetricsList(String rootIRI, List<Instance> instances) {

        var list = ul();

        list.with(li("Number of instances: " + instances.size()));

        {
            var signatureMap = instances.stream()
                .collect(Collectors.collectingAndThen(groupingBy(Instance::getIri), TreeMap::new));

            list.with(
                li(text("Templates used: (" + signatureMap.keySet().size() + " templates)"),
                    ul(
                        each(signatureMap, (k, v) -> li(getRelativeA(k, rootIRI), text(" (" + v.size() + ")")))
                    )
                ));
        }

        return list;
    }

    public Map<String, List<Resource>> getVocabularyMap(List<Instance> instances) {
        return instances.stream()
            .map(Instance::getArguments)
            .reduce(new ArrayList<>(), (a, b) -> {
                a.addAll(b);
                return a;
            })
            .stream()
            .map(Argument::getTerm)
            .filter(term -> term instanceof IRITerm)
            .map(term -> ((IRITerm) term).getIri())
            .filter(iri -> !iri.startsWith(OTTR.ns_example_arg))
            .distinct()
            .sorted()
            .map(ResourceFactory::createResource)
            .map(Resource::asResource)
            .collect(groupingBy(Resource::getNameSpace));
    }

    private ContainerTag getVocabularyMetricsList(List<Instance> instances) {

        var list = ul();
        var vocabularyElements = getVocabularyMap(instances);

        list.with(
            li(text("Namespaces introduced: (" + vocabularyElements.size() + ")"),
                ul(each(vocabularyElements.keySet(), ns ->
                    li(b(Objects.toString(this.prefixMapping.getNsURIPrefix(ns), "")).withClass("heading"),
                        text(": "), code(ns), ul(
                            each(vocabularyElements.get(ns), iri -> li(getA(iri.getURI())))))))));
        return list;

    }

    private DomContent writeSerialisations(Signature signature) {
        return div(
            getTOCHeading("Serialisations"),
            div(
                h4("stOTTR"),
                pre(this.serialisationWriter.writeStottr(signature)),
                h4("RDF/wOTTR"),
                pre(this.serialisationWriter.writeWottr(signature)))
        ).withId("serialisations");
    }

    private ContainerTag getHead(Signature signature) {
        return HTMLFactory.getHead(signature.getClass().getSimpleName()
            + ": " + RDFNodeWriter.toString(this.prefixMapping, signature.getIri()));
    }

    private DomContent getA(String iri) {
        var a = a(this.shortenIRI(iri));
        if (!OTTR.BaseURI.ALL.contains(iri)) {
            a.withHref(iri);
        }
        return a;
    }

    private DomContent getRelativeA(String iri, String rootIRI) {

        var a = a(this.shortenIRI(iri));
        if (!OTTR.BaseURI.ALL.contains(iri)) {
            a.withHref(DocttrManager.toLocalFilePath(iri, rootIRI, 1));
        }
        return a;
    }

    private String shortenIRI(String iri) {
        return RDFNodeWriter.toString(this.prefixMapping, iri);
    }

    private Model getExpansion(Instance instance) {
        WInstanceWriter instanceWriter = new WInstanceWriter(this.prefixMapping);
        this.expander.expandInstance(instance)
            .innerForEach(instanceWriter);
        return instanceWriter.writeToModel();
    }

}
