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

import static guru.nidi.graphviz.model.Factory.node;

import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.Node;
import org.apache.jena.shared.PrefixMapping;

public class DependencyGraphVisualiser extends GraphVisualiser {

    public DependencyGraphVisualiser(PrefixMapping prefixMapping) {
        super(prefixMapping);
    }

    public String drawTree(Tree<String> tree) {
        MutableGraph graph = getGraph()
            .graphAttrs().add(Rank.dir(Rank.RankDir.TOP_TO_BOTTOM));

        tree.preorderStream()
            .distinct()
            .forEach(signature -> signature.getChildren().stream()
                .forEach(child -> graph.add(uriNode(signature.getRoot()).link(uriNode(child.getRoot())))));

        return renderSVG(graph);
    }

    private Node uriNode(String uri) {
        return node(shortenURI(uri))
            .with("URL", uri);
    }

}
