package xyz.ottr.lutra.docttr.visualisation;

/*-
 * #%L
 * xyz.ottr.lutra:lutra-docttr
 * %%
 * Copyright (C) 2018 - 2020 University of Oslo
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

import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.engine.Engine;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.MutableGraph;
import j2html.tags.DomContent;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.writer.RDFNodeWriter;

public abstract class GraphVisualiser {

    private final PrefixMapping prefixMapping;
    private static final int TOTALMEMORY = 100000000;

    GraphVisualiser(PrefixMapping prefixMapping) {
        this.prefixMapping = prefixMapping;
    }

    protected MutableGraph getGraph() {
        return Factory.graph()
            .directed()
            .strict()
            // graph attr
            .graphAttr().with("center", "true")
            .graphAttr().with("overlap", "false")
            .graphAttr().with("splines", "true")
            .graphAttr().with("nodesep", "0.35")
            .graphAttr().with("ranksep", "0.35")
            // node attrs
            .nodeAttr().with("shape", "box")
            .nodeAttr().with("fontname", "Arial")
            .nodeAttr().with("fontsize", "14px")
            .nodeAttr().with("height", "0")
            .nodeAttr().with("width", "0")
            // link attrs
            .linkAttr().with("fontname", "Arial")
            .linkAttr().with("fontsize", "14px")
            .toMutable();
    }

    String shortenURI(String uri) {
        return RDFNodeWriter.toString(this.prefixMapping, uri);
    }

    static String renderSVG(MutableGraph graph) {
        return renderSVG(Engine.DOT, graph);
    }

    static String renderSVG(Engine engine, MutableGraph graph) {
        return Graphviz.fromGraph(graph).totalMemory(TOTALMEMORY).engine(engine).render(Format.SVG).toString();
    }

    static DomContent renderAllEngines(MutableGraph graph) {
        return div(
            details(
                summary("Hierarchical horizontal layout (dot)"),
                div(rawHtml(renderSVG(Engine.DOT, graph)))
            ).attr("open", "open"),
            details(
                summary(text("Hierarchical vertical layout (dot)")),
                div(rawHtml(renderSVG(Engine.DOT, graph.graphAttrs().add("rankdir", Rank.RankDir.TOP_TO_BOTTOM))))),
            details(
                summary("Spring model layout (neato)"),
                div(rawHtml(renderSVG(Engine.NEATO, graph)))),
            details(
                summary("Spring model layout (fdp)"),
                div(rawHtml(renderSVG(Engine.FDP, graph)))),
            details(
                summary("Radial layout (twopi)"),
                div(rawHtml(renderSVG(Engine.TWOPI, graph)))),
            details(
                summary("Circular layout (circo)"),
                div(rawHtml(renderSVG(Engine.CIRCO, graph))))
        );
    }

}
