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

import j2html.tags.ContainerTag;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.stottr.writer.STemplateWriter;
import xyz.ottr.lutra.wottr.writer.WTemplateWriter;
import xyz.ottr.lutra.writer.TemplateWriter;

public class DTemplateWriter implements TemplateWriter {

    private final Map<String, Signature> signatures;

    private final STemplateWriter stottrWriter;
    private final WTemplateWriter wottrWriter;

    private PrefixMapping prefixMapping;

    //private final RDFVizler vizler;

    public DTemplateWriter() {
        this(OTTR.getDefaultPrefixes());
    }

    public DTemplateWriter(PrefixMapping prefixMapping) {

        this.prefixMapping  = prefixMapping;

        this.signatures = new HashMap<>();
        this.stottrWriter = new STemplateWriter(prefixMapping);
        this.wottrWriter = new WTemplateWriter(prefixMapping);

        //this.vizler = new RDFVizler();
        //this.vizler.setRulesPath("src/main/resources/ottr-1.jrule");
        //this.vizler.setRulesPath("../rdfvizler/docs/rules/rdf.jrule");
    }

    @Override
    public Set<String> getIRIs() {
        return this.signatures.keySet();
    }

    @Override
    public void accept(Signature signature) {
        this.signatures.put(signature.getIri(), signature);
        this.stottrWriter.accept(signature);
        this.wottrWriter.accept(signature);
    }

    @Override
    public String write(String iri) {
        return document(write(this.signatures.get(iri)));
    }

    private ContainerTag write(Signature signature) {

        String signatureIRI = signature.getIri();

        String heading = signature.getClass().getSimpleName() + ": " + signatureIRI;
        String stottr = this.stottrWriter.write(signatureIRI);
        String wottr = this.wottrWriter.write(signatureIRI);

        List<String> dependencies = Collections.EMPTY_LIST;
        if (signature instanceof Template) {
            dependencies = ((Template) signature).getPattern().stream()
                .map(Instance::getIri)
                .sorted()
                .collect(Collectors.toList());
        }

        //String svgVisualisation = getVisualisation(signature);

        return html(
            head(
                title(heading),
                link().withRel("stylesheet").withHref("/css/main.css")
            ).withLang("en"),
            body(

                h1(heading),
                div(

                    iff(!dependencies.isEmpty(),
                    div(
                        h3("Direct dependencies"),
                        ul(each(dependencies, dep ->
                            li(a(this.prefixMapping.shortForm(dep)).withHref(dep)))
                        )
                    )),

                    div(
                        h2("stOTTR Serialisation"),
                        pre(stottr)),

                    //h2("Pattern"),

                    //h3("Visualisation"),
                    //rawHtml(svgVisualisation),

                    div(
                        h2("wOTTR Serialisation"),
                        pre(wottr))
                )
            )
        );

    }

    /*
    private String getVisualisation(Signature signature) {
        WInstanceWriter rdfWriter = new WInstanceWriter();

        this.graph.makeSkeletonInstanceOf(signature.getIRI())
            .mapToStream(this.graph::expandInstance)
            .innerForEach(rdfWriter);

        return getVisualisation(rdfWriter.writeToModel());
    }

    private String getVisualisation(Model pattern) {
        try {
            String svg = this.vizler.writeDotImage(pattern, DotProcess.ImageOutputFormat.svg);
            // remove doctype from svg image, so that the svg can be inserted inline.
            svg = svg.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
                + "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\"\n"
                + " \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">","");
            return svg;
        } catch (IOException e) {
            return pre(e.getLocalizedMessage()).render();
        }
    }*/

}
