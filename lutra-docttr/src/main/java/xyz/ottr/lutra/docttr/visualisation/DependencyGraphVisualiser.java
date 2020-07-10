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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.docttr.DocttrManager;
import xyz.ottr.lutra.docttr.Tree;
import xyz.ottr.lutra.store.TemplateStore;

public class DependencyGraphVisualiser extends GraphVisualiser {

    public DependencyGraphVisualiser(PrefixMapping prefixMapping) {
        super(prefixMapping);
    }

    protected MutableGraph getGraph() {
        return super.getGraph()
            .graphAttrs()
            .add(Rank.dir(Rank.RankDir.LEFT_TO_RIGHT));
    }

    // transitively draw all dependencies from iris
    public String drawGraph(String root, Collection<String> iris, TemplateStore store) {

        var graph = getGraph();

        Stack<String> visit = new Stack<>();
        visit.addAll(iris);
        Set<String> visited = new HashSet<>();

        while (!visit.isEmpty()) {
            var iri = visit.pop();
            if (!visited.contains(iri)) {
                visited.add(iri);
                store.getDependencies(iri).ifPresent(
                    deps -> deps
                        .forEach(child -> {
                            visit.add(child);
                            graph.add(uriNode(iri, root, 0).addLink(uriNode(child, root, 0)));
                        })
                );
            }
        }

        return renderSVG(graph);
    }

    public String drawTree(Tree<String> tree) {

        var graph = getGraph();

        var rootFolder = tree.getRoot();

        tree.preorderStream()
            .distinct()
            .forEach(signature -> signature.getChildren()
                .forEach(child -> graph.add(
                    uriNode(signature.getRoot(), rootFolder, 1)
                        .addLink(uriNode(child.getRoot(), rootFolder, 1)))));

        return renderSVG(graph);
    }


    /*
        Get node with url relative to root.
    */
    private MutableNode uriNode(String uri, String root, int parents) {
        var node = Factory.mutNode(shortenURI(uri));
        if (!OTTR.BaseURI.ALL.contains(uri)) {
            node.add("URL", DocttrManager.toLocalFilePath(uri, root, parents));
        }
        return node;
    }

}
