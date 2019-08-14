package xyz.ottr.lutra.wottr.parser.v03.util;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.ottr.lutra.wottr.util.RDFNodes;

public enum ModelSelector {

    ; // singleton enum utility class

    private static final Logger log = LoggerFactory.getLogger(ModelSelector.class);

    public static Set<String> getNamespaces(Model model) {
        Set<String> namespaces = new HashSet<>();
        for (Resource uri : ModelSelector.getURIResources(model)) {
            namespaces.add(uri.getNameSpace());
        }
        return namespaces;
    }

    public static Literal getOptionalLiteralOfProperty(Model model, Resource subject, Property property)
            throws ModelSelectorException {
        RDFNode object = getOptionalObjectOfProperty(model, subject, property);
        if (object != null && !object.isLiteral()) {
            throw new ModelSelectorException(
                    "Error getting optional property literal value '" + qname(model, property) + "' for subject '"
                            + qname(model, subject) + "'. Expected literal, but found " + object.toString());
        }
        log.trace("Found " + (object == null ? "0" : "1") + " property literal value(s) '" + qname(model, property)
                + "' for subject '" + qname(model, subject) + "'");
        return object == null ? null : object.asLiteral();
    }

    public static RDFNode getOptionalObjectOfProperty(Model model, Resource subject, Property property)
            throws ModelSelectorException {
        List<RDFNode> objects = listObjectsOfProperty(model, subject, property);
        int size = objects.size();
        if (size > 1) {
            throw new ModelSelectorException("Error getting optional property value '" + qname(model, property)
                    + "' for subject '" + qname(model, subject) + "'. Expected optionally 1 instance, but found "
                    + objects.size() + ": " + qname(model, objects));
        }
        log.trace("Found " + objects.size() + " property value(s) '" + qname(model, property) + "' for subject '"
                + qname(model, subject) + "'");
        return objects.isEmpty() ? null : objects.get(0);
    }

    public static Resource getOptionalResourceOfProperty(Model model, Resource subject, Property property)
            throws ModelSelectorException {
        RDFNode object = getOptionalObjectOfProperty(model, subject, property);
        if (object != null && !object.isResource()) {
            throw new ModelSelectorException(
                    "Error getting optional property resource value '" + qname(model, property) + "' for subject '"
                            + qname(model, subject) + "'. Expected resource, but found " + qname(model, object));
        }
        log.trace("Found " + (object == null ? "0" : "1") + " property resource value(s) '" + qname(model, property)
                + "' for subject '" + qname(model, subject) + "'");
        return object == null ? null : object.asResource();
    }

    /////////////////////////////////////
    // wrapper methods

    public static Statement getOptionalStatementWithProperties(Model model, Resource subject,
            Collection<Property> properties) throws ModelSelectorException {
        Set<Statement> statements = new HashSet<>();
        for (Property p : properties) {
            statements.addAll(model.listStatements(subject, p, (RDFNode) null).toSet());
        }
        return !statements.isEmpty() ? statements.iterator().next() : null;
    }

    ////////////////////////////////////
    // class instances

    public static Set<? extends RDFNode> getRDFNodes(Collection<Statement> statements) {
        Set<RDFNode> nodes = new HashSet<>();
        for (Statement t : statements) {
            nodes.add(t.getSubject());
            nodes.add(t.getPredicate());
            nodes.add(t.getObject());
        }
        return nodes;
    }

    @SuppressWarnings("unchecked")
    public static Set<RDFNode> getRDFNodes(Model model) {
        return (Set<RDFNode>) getRDFNodes(model.listStatements().toList());
    }

    ////////////////////////////////////
    // lists of subject-property objects

    public static Resource getRequiredInstanceOfClass(Model model, Resource cls) throws ModelSelectorException {
        List<Resource> individuals = listInstancesOfClass(model, cls);
        int size = individuals.size();
        if (size != 1) {
            throw new ModelSelectorException(
                    "Error getting instance of class " + qname(model, cls) + ". Expected exactly 1 instance, but found "
                            + individuals.size() + ": " + qname(model, individuals));
        }
        return individuals.get(0);
    }

    public static Literal getRequiredLiteralOfProperty(Model model, Resource subject, Property property)
            throws ModelSelectorException {
        RDFNode object = getRequiredObjectOfProperty(model, subject, property);
        if (!object.isLiteral()) {
            throw new ModelSelectorException(
                    "Error getting required property literal value '" + qname(model, property) + "' for subject '"
                            + qname(model, subject) + "'. Expected literal, but found " + qname(model, object));
        }
        return object.asLiteral();
    }

    ////////////////////////////////////
    // singletons of subject-property objects

    // required-s

    public static RDFNode getRequiredObjectOfProperty(Model model, Resource subject, Property property)
            throws ModelSelectorException {
        List<RDFNode> objects = listObjectsOfProperty(model, subject, property);
        int size = objects.size();
        if (size != 1) {
            throw new ModelSelectorException("Error getting required property value '" + qname(model, property)
                    + "' for subject '" + qname(model, subject) + "'. Expected exactly 1 instance, but found "
                    + objects.size() + ": " + qname(model, objects));
        }
        return objects.get(0);
    }

    public static Resource getRequiredResourceOfProperty(Model model, Resource subject, Property property)
            throws ModelSelectorException {
        RDFNode object = getRequiredObjectOfProperty(model, subject, property);
        if (!object.isResource()) {
            throw new ModelSelectorException(
                    "Error getting required property literal value '" + qname(model, property) + "' for subject '"
                            + qname(model, subject) + "'. Expected resource, but found " + qname(model, object));
        }
        return object.asResource();
    }

    public static Statement getRequiredStatementWithProperties(Model model, Resource subject,
            Collection<Property> properties) throws ModelSelectorException {
        Set<Statement> statements = new HashSet<>();
        for (Property p : properties) {
            statements.addAll(model.listStatements(subject, p, (RDFNode) null).toSet());
        }
        if (statements.size() != 1) {
            throw new ModelSelectorException("Error getting exactly one required value for properties: '",
                    qname(model, properties), "' and subject '", qname(model, subject),
                    "'. Expected resource, but found ",
                    // Strings.toString(statements, s -> qname(model, s.getObject()), ", "));
                    statements.stream().map(s -> qname(model, s.getObject())).collect(Collectors.joining(", ")));

        }
        return statements.iterator().next();
    }

    // special

    @SuppressWarnings("unchecked")
    public static Set<Resource> getURIResources(Model model) {
        Set<? extends RDFNode> nodes = getRDFNodes(model);
        nodes.removeIf(node -> !node.isURIResource());
        return (Set<Resource>) nodes;
    }

    // optionals

    public static List<Resource> listInstancesOfClass(Model model, Resource cls) {
        List<Resource> instances = model.listResourcesWithProperty(RDF.type, cls).toList();
        log.trace("Found " + instances.size() + " instance(s) of class " + qname(model, cls) + ": "
                + instances.toString());
        return instances;
    }

    public static List<RDFNode> listObjectsOfProperty(Model model, Resource subject, Property property) {
        List<RDFNode> objects = subject.getModel().listObjectsOfProperty(subject, property).toList();
        log.trace("Found " + objects.size() + " resource object(s) of subject " + qname(model, subject)
                + " and property " + qname(model, property) + ": " + qname(model, objects));
        return objects;
    }

    public static List<Resource> listResourcesOfProperty(Model model, Resource subject, Property property)
            throws ModelSelectorException {
        List<Resource> resources = new ArrayList<>();
        for (RDFNode object : listObjectsOfProperty(model, subject, property)) {
            if (!object.isResource()) {
                throw new ModelSelectorException("Error getting resource objects of subject " + qname(model, subject)
                        + " and property " + qname(model, property) + ". Excepted resource node, but found: "
                        + qname(model, object));
            }
            resources.add(object.asResource());
        }
        return resources;
    }

    public static List<Resource> listResourcesWithProperty(Model model, Property property) {
        return model.listResourcesWithProperty(property).toList();
    }
    
    
    /**
     * Selects all triples where the triple subject is part of the linked list
     * structure.
     *
     * @param head
     * @return
     */
    public static List<Statement> getAllListStatements(RDFList head) {
        List<Statement> list = new ArrayList<>();
        Model model = head.getModel();
        Resource first = head;

        for (ExtendedIterator<RDFNode> it = head.iterator(); it.hasNext();) {
            list.addAll(model.listStatements(first, null, (RDFNode) null).toList());

            // recursive call, if content is a list
            RDFNode content = it.next();
            if (content.canAs(RDFList.class)) {
                list.addAll(getAllListStatements(content.as(RDFList.class)));
            }
            // prepare for next list element
            if (it.hasNext()) {
                first = model.getRequiredProperty(first, RDF.rest).getObject().asResource();
            }
        }
        return list;
    }

    //////////////////////////////////////////////
    // privates

    private static String qname(Model model, Collection<? extends RDFNode> nodes) {
        String list = nodes.stream().map(n -> RDFNodes.toString(model, n)).collect(Collectors.joining(", "));
        return "[" + list + "]";
    }

    private static String qname(Model model, List<? extends RDFNode> nodes) {
        return RDFNodes.toString(model, nodes);
    }

    private static String qname(Model model, RDFNode node) {
        return RDFNodes.toString(model, node);
    }
}
