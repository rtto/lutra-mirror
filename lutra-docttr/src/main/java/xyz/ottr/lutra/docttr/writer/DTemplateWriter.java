package xyz.ottr.lutra.docttr.writer;

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

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import java.io.InputStream;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Setter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.PrefixMapping;
import xyz.dyreriket.rdfvizler.RDF2DotParser;
import xyz.dyreriket.rdfvizler.RDFVizler;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.io.Format;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.Substitution;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.stottr.writer.SInstanceWriter;
import xyz.ottr.lutra.stottr.writer.STemplateWriter;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.wottr.io.RDFIO;
import xyz.ottr.lutra.wottr.util.PrefixMappings;
import xyz.ottr.lutra.wottr.writer.WInstanceWriter;
import xyz.ottr.lutra.wottr.writer.WTemplateWriter;
import xyz.ottr.lutra.writer.RDFNodeWriter;
import xyz.ottr.lutra.writer.TemplateWriter;


// TODO docttr is not a format. make it take a (collection of) signatures?
public class DTemplateWriter implements TemplateWriter, Format {

    private static final String ROOT_resources = "/treeview/";

    private static final String name = "docttr";
    private static final Collection<Support> support = Set.of(Support.TemplateWriter);

    private final Map<String, Signature> signatures;

    private final TemplateStore templateStore;

    @Setter private PrefixMapping prefixMapping;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss z", Locale.ENGLISH)
        .withZone(ZoneOffset.UTC);


    // TODO replace with graphviz lib
    private final RDFVizler vizler;
    private static final String vizlerRules = "rdfvizler/ottr-2.jrule";
    private static final String dependenciesRules = "rdfvizler/dependencies.jrule";

    public DTemplateWriter(TemplateStore templateStore, PrefixMapping prefixMapping) {

        this.prefixMapping = prefixMapping;
        this.prefixMapping.withDefaultMappings(OTTR.getStandardLibraryPrefixes());
        this.prefixMapping.setNsPrefix("x", OTTR.ns_example_arg);

        this.templateStore = templateStore;

        this.signatures = new HashMap<>();

        this.vizler = new RDFVizler();
    }

    @Override
    public Result<TemplateWriter> getTemplateWriter() {
        return Result.of(this);
    }

    @Override
    public Collection<Support> getSupport() {
        return support;
    }

    @Override
    public String getDefaultFileSuffix() {
        return ".html";
    }

    @Override
    public String getFormatName() {
        return name;
    }

    @Override
    public Set<String> getIRIs() {
        return this.signatures.keySet();
    }

    @Override
    public void accept(Signature signature) {
        this.signatures.put(signature.getIri(), signature);
    }

    @Override
    public String write(String iri) {
        return document(write(this.signatures.get(iri)));
    }


    private ContainerTag write(Signature signature) {

        return html(
            writeHead(signature),
            body(
                h1(getHeading(signature)),
                div(
                    div(
                        p(
                            text("URI: "),
                            code(a(signature.getIri()).withHref(signature.getIri()))
                        ),
                        writeStottrSerialisation(signature)
                    ),

                    writePattern(signature),
                    writeDependencies(signature),
                    writeSerialisations(signature),

                    h2("Prefixes"),
                    info("Prefixes are removed from all listings on this page for readability, "
                        + "but are listed here in RDF Turtle format."),
                    writePrefixes()
                    ),
                writeFooter(),
                writeScripts()
            )
        );
    }

    private ContainerTag writePattern(Signature signature) {

        var exampleInstance = signature.getExampleInstance();
        var exampleExpansion = this.getExampleExpansion(exampleInstance);
        var wexampleInstance = this.getWInstanceModel(exampleInstance);

        return div(
            h2(text("Pattern")),
            info("The pattern of the template is illustrated by expanding a generated instance. "
                + "Below the generated instance is shown in different serialisations,"
                + " and its expansion is presented in different formats."),
            h4("Generated instance"),
            p("stOTTR"),
            pre(printSInstance(exampleInstance)),
            p("RDF/wOTTR"),
            pre(removePrefixes(RDFIO.writeToString(PrefixMappings.trim(wexampleInstance)))),
            h4("Interactive expansion"),
            info("Click each instance to expand it."),
            div(writeInteractiveExpansion(exampleInstance)),
            h4("Visualisation of expanded RDF graph"),
            info("Each resource node is linked to its IRI."),
            div(rawHtml(getVisualisation(exampleExpansion, vizlerRules))),
            h4("Expanded RDF graph"),
            pre(removePrefixes(RDFIO.writeToString(PrefixMappings.trim(exampleExpansion))))
        );
    }


    public ContainerTag writeInteractiveExpansion(Instance exampleInstance) {

        Function<Instance, List<Instance>> builder = instance -> {
            var signature = this.templateStore.getTemplateSignature(instance.getIri()).get();
            // TODO: create a substitution from a Map directly to avoid validation
            var substitution = Substitution.resultOf(instance.getArguments(), signature.getParameters()).get();
            return this.templateStore.getTemplate(instance.getIri()).get().getPattern().stream()
                .map(is -> is.apply(substitution))
                .collect(Collectors.toList());
        };

        var expansionTree = new Tree<>(exampleInstance, builder);

        var toListElement = new Tree.Action<Instance, ContainerTag>() {
            @Override
            public ContainerTag perform(Tree<Instance> tree) {

                var instance = code(printSInstance(tree.getRoot()));
                var children = tree.getChildren();
                children.sort(Comparator.comparing(a -> a.getRoot().getIri()));

                return tree.hasChildren()
                    ? li(instance.withClasses("template", "click"), ul(each(children, this::perform)))
                    : li(instance.withClass("baseTemplate"));
            }
        };

        return ul()
            .with(expansionTree.apply(toListElement))
            .withClass("treeview");
    }

    private DomContent writeDependencies(Signature signature) {
        return div(
            h2("Dependencies"),
            writeDirectDependencies(signature),
            h4("Dependency graph"),
            info("The graph shows all the templates that this template depends on. Each node is linked to the template IRI."),
            rawHtml(getVisualisation(getAllModels(signature), dependenciesRules))
        );
    }

    private DomContent writeSerialisations(Signature signature) {
        return div(
            h2("Serialisations"),
            div(
                h4("stOTTR"),
                writeStottrSerialisation(signature),
                h4("RDF/wOTTR"),
                writeWtottrSerialisation(signature))
        );
    }


    private String getHeading(Signature signature) {
        return signature.getClass().getSimpleName() + ": " + RDFNodeWriter.toString(this.prefixMapping, signature.getIri());
    }

    private ContainerTag writeHead(Signature signature) {
        return DFramesWriter.getHead()
            .with(
                title(getHeading(signature)),
                styleWithInlineFile(ROOT_resources + "treeview.css")
            );
    }

    private DomContent writeScripts() {

        return join(
            // TODO put in js file
            scriptWithInlineFile(ROOT_resources + "treeview.js"),
            // TODO is this needed with new graphviz lib? if it is, then move to js file.
            // remove width and height to make svg scalable.
            script(rawHtml(
                "var svgs = document.getElementsByTagName('svg');"
                    + "for (var svg of svgs) { "
                    + "svg.removeAttribute('width'); "
                    + "svg.removeAttribute('height'); "
                    + "svg.style.maxWidth = '1000px'; "
                    + "}"
            ))
        );
    }

    private ContainerTag writePrefixes() {
        return pre(this.prefixMapping.getNsPrefixMap().entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .map(e -> "@prefix " + String.format(Locale.ENGLISH, "%-12s", e.getKey() + ":") + " <" + e.getValue() + "> .")
            .collect(Collectors.joining("\n")));
    }

    // TODO use new rependency map
    private Map<String, List<Instance>> getDependencyMap(Signature signature) {

        Map<String, List<Instance>> dependencyMap = Collections.EMPTY_MAP;
        if (signature instanceof Template) {
            dependencyMap = ((Template) signature).getPattern()
                .stream()
                .collect(groupingBy(Instance::getIri));
        }
        return new TreeMap(dependencyMap); // sort keys
    }

    // TODO use new rependency map
    private ContainerTag writeDirectDependencies(Signature signature) {

        var dependencyMap = getDependencyMap(signature); // sort keys
        return
            iff(!dependencyMap.isEmpty(),
                div(
                h4("Direct dependencies"),
                    info("The direct dependencies are the templates directly instantiated by this template. "
                        + "The number in parenthesis is the number of instances of each template."),
                    ul(each(dependencyMap, (key, value) ->
                        li(a(this.prefixMapping.shortForm(key)).withHref(key),
                            text(" "),
                            span("(" + value.size() + ")")
                        )
                    )
                )));
    }


    // TODO delete this when graphviz library is in place
    private Model getAllModels(Signature signature) {

        var visit = new Stack<Signature>();
        var visited = new HashSet<Signature>();

        var templateWriter = new WTemplateWriter(this.prefixMapping);
        var allModel = ModelFactory.createDefaultModel();

        visit.add(signature);

        while (!visit.isEmpty()) {
            var current = visit.pop();
            if (!visited.contains(current)) {
                visited.add(current);
                templateWriter.accept(current);
                allModel.add(templateWriter.getModel(current));
                getDependencyMap(current).keySet().forEach(iri -> this.templateStore.getTemplate(iri).ifPresent(visit::push));
            }
        }
        return allModel;
    }

    private ContainerTag writeStottrSerialisation(Signature signature) {
        var writer = new STemplateWriter(this.prefixMapping);
        writer.accept(signature);
        return pre(writer.writeSignature(signature, false));
    }

    private ContainerTag writeWtottrSerialisation(Signature signature) {
        var writer = new WTemplateWriter(this.prefixMapping);
        writer.accept(signature);
        return pre(removePrefixes(writer.write(signature.getIri())));
    }

    private DomContent info(String description) {
        return p(rawHtml("&#128712; "), text(description))
            .withClass("info");
    }

    private DomContent writeFooter() {
        return div(
            p(text("This is the documentation page for an OTTR template. "
                + "For more information about Reasonable Ontology Templates (OTTR), visit "),
            a("ottr.xyz").withHref("http://ottr.xyz"),
            text(".")),
            p(text("Generated: "), text(this.dtf.format(ZonedDateTime.now()))))
            .withClass("footer");
    }

    private Model getExampleExpansion(Instance instance) {
        WInstanceWriter instanceWriter = new WInstanceWriter(this.prefixMapping);
        this.templateStore.expandInstanceWithoutChecks(instance)
            .innerForEach(instanceWriter);
        return instanceWriter.writeToModel();
    }

    private String printSInstance(Instance instance) {
        var writer = new SInstanceWriter(this.prefixMapping);
        writer.accept(instance);
        return writer.writeInstance(instance);
    }

    private Model getWInstanceModel(Instance instance) {
        var writer = new WInstanceWriter(this.prefixMapping);
        writer.accept(instance);
        return writer.writeToModel();
    }


    // Move to own class
    private String getVisualisation(Model pattern, String rulePath) {

        var rules = this.vizler.getRules(getResourceAsStream(rulePath));
        Model dotModel = this.vizler.getRDFDotModel(pattern, rules);
        String dot = new RDF2DotParser(dotModel).toDot();
        return this.vizler.getDotImage(dot, "SVG");
    }

    private String removePrefixes(String turtleRDFModel) {
        return Arrays.asList(turtleRDFModel.split("\\n")).stream()
            .filter(s -> !s.startsWith("@prefix "))
            .collect(Collectors.joining("\n"));
    }

    private InputStream getResourceAsStream(String path) {
        return this.getClass().getClassLoader().getResourceAsStream(path);
    }

}
