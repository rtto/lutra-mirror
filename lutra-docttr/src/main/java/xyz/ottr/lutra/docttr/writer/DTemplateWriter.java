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
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import lombok.Setter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.PrefixMapping;
import xyz.dyreriket.rdfvizler.DotProcess;
import xyz.dyreriket.rdfvizler.RDF2DotParser;
import xyz.dyreriket.rdfvizler.RDFVizler;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.io.Format;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.stottr.writer.SInstanceWriter;
import xyz.ottr.lutra.stottr.writer.STemplateWriter;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.wottr.io.RDFIO;
import xyz.ottr.lutra.wottr.util.RDFNodes;
import xyz.ottr.lutra.wottr.writer.WInstanceWriter;
import xyz.ottr.lutra.wottr.writer.WTemplateWriter;
import xyz.ottr.lutra.writer.TemplateWriter;

public class DTemplateWriter implements TemplateWriter, Format {

    private static final String name = "docttr";
    private static final Collection<Support> support = Set.of(Support.TemplateWriter);

    private final Map<String, Signature> signatures;

    private final STemplateWriter stemplateWriter;
    private final WTemplateWriter wtemplateWriter;

    private final TemplateStore templateStore;

    @Setter private PrefixMapping prefixMapping;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss z", Locale.ENGLISH)
        .withZone(ZoneOffset.UTC);

    private final RDFVizler vizler;
    private static final String vizlerRules = "rdfvizler/ottr-2.jrule";

    public DTemplateWriter(TemplateStore templateStore, PrefixMapping prefixMapping) {

        this.prefixMapping = prefixMapping;
        this.prefixMapping.withDefaultMappings(OTTR.getStandardLibraryPrefixes());
        this.prefixMapping.setNsPrefix("x", OTTR.ns_example_arg);

        this.templateStore = templateStore;

        this.signatures = new HashMap<>();

        this.stemplateWriter = new STemplateWriter(this.prefixMapping);
        this.wtemplateWriter = new WTemplateWriter(this.prefixMapping);

        this.vizler = new RDFVizler();
    }

    public void setDotPath(String path) {
        this.vizler.setDotExecutable(path);
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
        this.stemplateWriter.accept(signature);
        this.wtemplateWriter.accept(signature);
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
                    writeDependencies(signature),
                    writeStottrSerialisation(signature),
                    writePattern(signature),
                    writeWtottrSerialisation(signature)
                ),
                writeFooter()
            )
        );
    }


    private String getHeading(Signature signature) {
        return signature.getClass().getSimpleName() + ": " + RDFNodes.toString(this.prefixMapping, signature.getIri());
    }

    private ContainerTag writeHead(Signature signature) {
        return head(
            title(getHeading(signature)),
            link().withRel("stylesheet").withHref("/css/main.css")
        ).withLang("en");
    }

    private ContainerTag writeDependencies(Signature signature) {

        Map<String, List<Instance>> dependencyMap = Collections.EMPTY_MAP;
        if (signature instanceof Template) {
            dependencyMap = ((Template) signature).getPattern()
                .stream()
                .collect(groupingBy(Instance::getIri));
        }
        dependencyMap = new TreeMap(dependencyMap); // sort keys

        return
            iff(!dependencyMap.isEmpty(),
                div(
                h3("Direct dependencies"),
                ul(each(dependencyMap, (key, value) ->
                        li(a(this.prefixMapping.shortForm(key)).withHref(key),
                            text(" "),
                            span("(" + value.size() + ")")
                        )
                    )
                )));
    }

    private ContainerTag writeStottrSerialisation(Signature signature) {
        return div(
            h2("stOTTR Serialisation"),
            pre(this.stemplateWriter.write(signature.getIri())));
    }

    private ContainerTag writeWtottrSerialisation(Signature signature) {
        return div(
            h2("wOTTR Serialisation"),
            pre(this.wtemplateWriter.write(signature.getIri())));
    }

    private ContainerTag writePattern(Signature signature) {

        var exampleInstance = signature.getExampleInstance();
        var exampleExpansion = this.getExampleExpansion(exampleInstance);
        var wexampleInstance = this.getWInstanceModel(exampleInstance);

        return div(
            h2("Pattern"),
            h3("Example instance"),
            pre(printSInstance(exampleInstance)),
            h3("Visualisation of the expansion of the example instance"),
            rawHtml(getVisualisation(exampleExpansion)),
            h3("The RDF graph of the expansion of the example instance"),
            pre(RDFIO.writeToString(exampleExpansion)),
            h3("Example instance in wOTTR"),
            pre(RDFIO.writeToString(wexampleInstance))
        );
    }

    private DomContent writeFooter() {
        return div(
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
        return writer.write();
    }

    private Model getWInstanceModel(Instance instance) {
        var writer = new WInstanceWriter(this.prefixMapping);
        writer.accept(instance);
        return writer.writeToModel();
    }


    private String getVisualisation(Model pattern) {
        try {

            var rules = this.vizler.getRules(getResourceAsStream(vizlerRules));
            Model dotModel = this.vizler.getRDFDotModel(pattern, rules);
            String dot = new RDF2DotParser(dotModel).toDot();
            String svg = this.vizler.getDotImage(dot, DotProcess.ImageOutputFormat.svg);

            // remove doctype from svg image, so that the svg can be inserted inline.
            svg = svg.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
                + "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\"\n"
                + " \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">","");
            return svg;
        } catch (IOException e) {
            return pre(e.getLocalizedMessage()).render();
        }
    }

    private InputStream getResourceAsStream(String path) {
        return this.getClass().getClassLoader().getResourceAsStream(path);
    }

}
