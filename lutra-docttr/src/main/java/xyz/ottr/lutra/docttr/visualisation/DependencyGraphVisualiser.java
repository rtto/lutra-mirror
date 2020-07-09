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

import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.docttr.HTMLIndexWriter;
import xyz.ottr.lutra.docttr.Tree;
import xyz.ottr.lutra.store.TemplateStore;

public class DependencyGraphVisualiser extends GraphVisualiser {

    public DependencyGraphVisualiser(PrefixMapping prefixMapping) {
        super(prefixMapping);
    }

    protected MutableGraph getGraph() {
        MutableGraph graph = super.getGraph()
            .graphAttrs()
            .add(Rank.dir(Rank.RankDir.LEFT_TO_RIGHT));

        return graph;
    }

    public String drawGraph(TemplateStore store) {

        var graph = getGraph();

        store.getTemplateIRIs().stream()
            .forEach(iri -> store.getDependencies(iri).ifPresent(
                deps -> deps.stream()
                .forEach(child ->
                    graph.add(uriNode(iri, null)
                        .addLink(uriNode(child, null))))));

        return renderSVG(graph);
    }

    public String drawTree(Tree<String> tree) {

        var graph = getGraph();

        tree.preorderStream()
            .distinct()
            .forEach(signature -> signature.getChildren().stream()
                .forEach(child -> graph.add(
                    uriNode(signature.getRoot(), tree.getRoot())
                        .addLink(uriNode(child.getRoot(), tree.getRoot())))));

        return renderSVG(graph);
    }


    /*
    Get node with url relative to root. Root can be null, in which the path becomes the standard

     */
    private MutableNode uriNode(String uri, String root) {
        var node = Factory.mutNode(shortenURI(uri));
        if (!uri.equals(OTTR.BaseURI.Triple) && !uri.equals(OTTR.BaseURI.NullableTriple)) {
            node.add("URL", HTMLIndexWriter.toLocalPath(uri, root));
        }
        return node;
    }

}
