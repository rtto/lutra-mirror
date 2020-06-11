package xyz.ottr.lutra.writer;

/*-
 * #%L
 * lutra-core
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

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import xyz.ottr.lutra.OTTR;

public enum RDFNodeWriter {

    ;

    public static String toString(Collection<?> collection) {
        return toString(collection.stream().map(RDFNodeWriter::toString));
    }

    public static String toString(Iterator<?> iterator) {
        var stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
        return toString(stream);
    }

    private static String toString(Stream<String> stream) {
        return stream
            .collect(Collectors.joining(", ", "[", "]"));
    }

    public static String toString(RDFNode node) {
        return toString(node.getModel(), node.asNode());
    }

    public static String toString(String uri) {
        return OTTR.getDefaultPrefixes().shortForm(uri);
    }

    public static String toString(Object object) {
        return toString(object.toString());
    }

    public static String toString(Model model, Collection<? extends RDFNode> nodes) {
        return toString(nodes.stream().map(node -> toString(model, node)));
    }

    public static String toString(Model model, RDFNode node) {
        return node.canAs(RDFList.class)
            ? toString(model, node.as(RDFList.class).asJavaList())
            : toString(model, node.asNode());
    }

    public static String toString(Model model, Node node) {
        return node.isVariable()
            ? node.toString()
            : toString(model, node.toString());
    }

    public static String toString(Model model, String nodeString) {
        return (model == null)
            ? toString(nodeString)
            : model.shortForm(nodeString);
    }
}
