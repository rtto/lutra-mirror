package xyz.ottr.lutra.docttr.writer;

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

import static guru.nidi.graphviz.model.Factory.graph;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.writer.RDFNodeWriter;

public abstract class GraphVisualiser {

    private final PrefixMapping prefixMapping;

    GraphVisualiser(PrefixMapping prefixMapping) {
        this.prefixMapping = prefixMapping;
    }

    protected MutableGraph getGraph() {
        return graph()
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

    protected String shortenURI(String uri) {
        return RDFNodeWriter.toString(this.prefixMapping, uri);
    }

    protected String renderSVG(MutableGraph graph) {
        return Graphviz.fromGraph(graph).render(Format.SVG).toString();
    }

}
