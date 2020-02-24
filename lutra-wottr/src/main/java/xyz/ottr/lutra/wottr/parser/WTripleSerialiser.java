package xyz.ottr.lutra.wottr.parser;

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
import java.util.List;
import java.util.function.Function;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import xyz.ottr.lutra.wottr.WOTTR;

public class WTripleSerialiser {

    private final Model model;

    public WTripleSerialiser(Model model) {
        this.model = model;
    }

    // TODO: select the most efficient datastructure
    private List<Statement> getCollection() {
        return new ArrayList<>();
    }

    public List<Statement> serialiseInstance(Resource instance) {
        List<Statement> triples = getCollection();

        triples.addAll(listStatements(instance));
        // get argument list, and the neighbourhood of each argument:
        triples.addAll(serialisePropertyList(instance, WOTTR.arguments, this::listStatements));

        // get value list, which may be a list of lists
        listStatements(instance, WOTTR.values).forEachRemaining(statement -> {
            Resource argList = statement.getObject().asResource();
            triples.addAll(serialiseList(argList, true));
        });

        return triples;
    }

    public List<Statement> serialiseTemplate(Resource template) {
        List<Statement> triples = getCollection();

        triples.addAll(listStatements(template));
        // get parameter list, and the (possibly complex) neighbourhood of each parameter:
        triples.addAll(serialisePropertyList(template, WOTTR.parameters, this::serialiseParameter));

        // serialise instances in ottr:pattern and ottr:annotation
        for (Property property : List.of(WOTTR.pattern, WOTTR.annotation)) {
            listStatements(template, property).forEachRemaining(statement ->
                triples.addAll(serialiseInstance(statement.getObject().asResource())));
        }
        return triples;
    }

    private List<Statement> serialiseParameter(Resource parameter) {
        List<Statement> triples = getCollection();

        triples.addAll(listStatements(parameter));

        // type and default value may be list of lists:
        for (Property property : List.of(WOTTR.type, WOTTR.defaultVal)) {
            listStatements(parameter, property).forEachRemaining(statement -> {
                if (statement.getObject().asResource().canAs(RDFList.class)) {
                    triples.addAll(serialiseList(statement.getObject().asResource(), true));
                }
            });
        }
        return triples;
    }

    /*
     * Serialise the list found as object of the triple (*resource*, *property*, _list_),
     * including the contents of the list, which are processed by the contentSerialiser
     * function. We assume that the object is a list and that it wellformed.
     */
    private List<Statement> serialisePropertyList(Resource resource, Property property,
                                                        Function<Resource, Collection<Statement>> contentSerialiser) {

        List<Statement> triples = getCollection();
        listStatements(resource, property).forEachRemaining(statement -> {
            Resource list = statement.getObject().asResource();
            triples.addAll(serialiseList(list, false));
            list.as(RDFList.class).asJavaList().forEach(item ->
                triples.addAll(contentSerialiser.apply(item.asResource()))
            );
        });
        return triples;
    }


    /**
     * Collect the linked list structure of an RDFlist, i.e., all rdf:first and rdf:rest triples,
     * including types of the blank nodes of the list structure. The method assumes that the list
     * is wellformed, i.e., exactly one rdf:first and rdf:rest per "link" in the list.
     * @param list the head of the list
     * @param isRecursive should we include lists inside the list?
     * @return a collection of the triples that make out the list
     */
    private List<Statement> serialiseList(Resource list, boolean isRecursive) {

        List<Statement> triples = getCollection();
        Resource head = list;
        while (!head.equals(RDF.nil)) {
            Statement first = listStatements(head, RDF.first).next();
            Statement rest = listStatements(head, RDF.rest).next();

            triples.addAll(listStatements(head, RDF.type).toList());
            triples.add(first);
            triples.add(rest);

            if (isRecursive && first.getObject().canAs(RDFList.class)) {
                triples.addAll(serialiseList(first.getObject().asResource(), isRecursive));
            }
            head = rest.getObject().asResource(); // move on to next node in the linked list
        }
        return triples;
    }


    ////////////////////
    // Util methods that avoids repetition of often used methods

    private List<Statement> listStatements(Resource resource) {
        return listStatements(resource, null).toList();
    }

    private StmtIterator listStatements(Resource resource, Property property) {
        return this.model.listStatements(resource, property, (RDFNode) null);
    }

}
