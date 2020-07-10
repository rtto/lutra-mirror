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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.Space;
import xyz.ottr.lutra.docttr.visualisation.DependencyGraphVisualiser;
import xyz.ottr.lutra.docttr.visualisation.TripleInstanceGraphVisualiser;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.Substitution;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.wottr.writer.WInstanceWriter;
import xyz.ottr.lutra.writer.RDFNodeWriter;

// TODO docttr is not a format. make it take a (collection of) signatures?
public class HTMLTemplateWriter {

    private final PrefixMapping prefixMapping;
    private final TemplateStore store;

    private final SerialisationWriter serialisationWriter;
    private final DependencyGraphVisualiser dependencyGraphVisualiser;

    public HTMLTemplateWriter(PrefixMapping prefixMapping, TemplateStore store) {
        this.prefixMapping = prefixMapping;
        this.prefixMapping.withDefaultMappings(OTTR.getStandardLibraryPrefixes());
        this.prefixMapping.setNsPrefix("x", OTTR.ns_example_arg);

        this.store = store;

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

        return html(
            getHead(signature),
            body(
                h1(join(
                    signature.getClass().getSimpleName() + ": ",
                    code(RDFNodeWriter.toString(this.prefixMapping, signature.getIri())))),
                div(
                    div(
                        p(
                            text("URI: "),
                            code(a(signature.getIri()).withHref(signature.getIri()))
                        ),
                        pre(this.serialisationWriter.writeStottr(signature))
                    ),

                    writePattern(signature),
                    writeDependencies(signature),
                    writeSerialisations(signature),

                    HTMLFactory.getPrefixDiv(this.prefixMapping)
                    ),
                HTMLFactory.getFooterDiv(),
                writeScripts()
            )
        );
    }

    private ContainerTag writePattern(Signature signature) {

        var exampleInstance = signature.getExampleInstance();
        var exampleExpansion = this.getExampleExpansion(exampleInstance);
        var wexampleInstance = this.serialisationWriter.writeWottrModel(exampleInstance);

        var expansionViz = new TripleInstanceGraphVisualiser(this.prefixMapping);
        this.store.expandInstanceWithoutChecks(exampleInstance)
            .innerForEach(expansionViz);

        return div(
            h2(text("Pattern")),
            HTMLFactory.getInfoP("The pattern of the template is illustrated by expanding a generated instance. "
                + "Below the generated instance is shown in different serialisations,"
                + " and its expansion is presented in different formats."),
            h4("Generated instance"),
            p("stOTTR"),
            pre(this.serialisationWriter.writeStottr(exampleInstance)),
            p("RDF/wOTTR"),
            pre(this.serialisationWriter.writeRDF(wexampleInstance)),
            h4("Visualisation of expanded RDF graph"),
            HTMLFactory.getInfoP("Each resource node is linked to its IRI."),
            div(rawHtml(expansionViz.draw(exampleInstance.getArguments()))),
            h4("Expanded RDF graph"),
            pre(this.serialisationWriter.writeRDF(exampleExpansion)),
            h4("Interactive expansion"),
            HTMLFactory.getInfoP("Click the list to expand/contract one list element. "
                + "Click 'expand/contact all' to expand/contract all elements. "
                + "Note that the interactive expansion is not correct for instances that are marked by list expanders."),
            div(writeInteractiveExpansion(exampleInstance))
        );
    }

    private ContainerTag writeInteractiveExpansion(Instance exampleInstance) {

        // Build expansion tree
        Function<Instance, List<Instance>> builder = instance -> {
            var signature = this.store.getTemplateSignature(instance.getIri()).get();
            // TODO: create a substitution from a Map directly to avoid validation
            var substitution = Substitution.resultOf(instance.getArguments(), signature.getParameters()).get();
            return this.store.getTemplate(instance.getIri()).get().getPattern().stream()
                .map(is -> is.apply(substitution))
                .collect(Collectors.toList());
        };
        var expansionTree = new Tree<>(exampleInstance, builder);

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

    private DomContent writeDependencies(Signature signature) {

        var tree = getDependencyTree(signature);

        return div(
            h2("Dependencies"),
            h4("Dependency graph"),
            HTMLFactory.getInfoP("The graph shows all the templates that this template depends on. "
                + "Each node is linked to its documentation page."),
            rawHtml(this.dependencyGraphVisualiser.drawTree(tree)),
            h4("List of dependencies"),
            HTMLFactory.getInfoP("The number in parenthesis is the number of instances of each template."),
            writeDependenciesList(tree)
        );
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

    private DomContent writeSerialisations(Signature signature) {
        return div(
            h2("Serialisations"),
            div(
                h4("stOTTR"),
                pre(this.serialisationWriter.writeStottr(signature)),
                h4("RDF/wOTTR"),
                pre(this.serialisationWriter.writeWottr(signature)))
        );
    }

    private ContainerTag getHead(Signature signature) {
        return HTMLFactory.getHead(signature.getClass().getSimpleName()
            + ": " + RDFNodeWriter.toString(this.prefixMapping, signature.getIri()));
    }

    private DomContent writeScripts() {
        return scriptWithInlineFile("/docttr.js");
    }

    private Model getExampleExpansion(Instance instance) {
        WInstanceWriter instanceWriter = new WInstanceWriter(this.prefixMapping);
        this.store.expandInstanceWithoutChecks(instance)
            .innerForEach(instanceWriter);
        return instanceWriter.writeToModel();
    }

}
