package xyz.ottr.lutra.wottr.util;

/*-
 * #%L
 * lutra-wottr
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

import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.system.Result;

public enum RDFNodes {

    ; // singleton enum

    private static final PrefixMapping defaultPrefixes = OTTR.getDefaultPrefixes();

    public static <X extends RDFNode> Result<X> cast(RDFNode node, Class<X> type) {

        if (node.canAs(type)) {
            return Result.of(node.as(type));
        } else {
            return Result.error("Expected instance of " + type.getSimpleName()
                + ", but found " + node.getClass().getSimpleName() + ": " + RDFNodes.toString(node));
        }
    }

    public static Result<Resource> castURIResource(RDFNode node) {
        Result<Resource> resource = cast(node, Resource.class);
        if (resource.isPresent() && !resource.get().isURIResource()) {
            return Result.error("Expected instance of URIResource, but got " +  RDFNodes.toString(resource.get()) + ".");
        } else {
            return resource;
        }
    }

    public static String toString(Collection<? extends RDFNode> nodes) {
        return nodes.stream()
            .map(RDFNodes::toString)
            .collect(Collectors.joining(", ", "[", "]"));
    }

    public static String toString(Model model, Collection<? extends RDFNode> nodes) {
        return nodes.stream()
            .map(node -> toString(model, node))
            .collect(Collectors.joining(", ", "[", "]"));
    }

    public static String toString(Model model, RDFNode node) {
        return node.canAs(RDFList.class)
            ? toString(model, node.as(RDFList.class).asJavaList())
            : toString(model, node.asNode());
    }

    public static String toString(RDFNode node) {
        return toString(node.getModel(), node.asNode());
    }

    public static String toString(Model model, Node node) {
        if (node.isVariable()) {
            return node.toString();
        } else {
            return toString(model, node.toString());
        }
    }

    public static String toString(Model model, String nodeString) {
        return (model == null)
            ? toString(nodeString)
            : model.shortForm(nodeString);
    }

    public static String toString(String nodeString) {
        return defaultPrefixes.shortForm(nodeString);
    }

}
