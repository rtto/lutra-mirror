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

import java.util.List;
import java.util.Optional;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import xyz.ottr.lutra.result.Result;

public enum ModelSelector {

    ; // singleton enum

    enum CardinalityOne {

        ZERO_ONE("optionally 1"),
        ONE("exactly 1"),
        ONE_MANY("at least 1");

        private final String text;

        CardinalityOne(String text) {
            this.text = text;
        }

        public String toString() {
            return this.text;
        }

    }

    public static List<Resource> getSubjects(Model model, Property property) {
        return model.listResourcesWithProperty(property).toList();
    }

    public static List<Result<Resource>> getResourceObjects(Model model, Resource subject, Property property) {

        return model.listStatements(subject, property, (RDFNode) null)
            .mapWith(Statement::getObject)
            .mapWith(o -> RDFNodes.cast(o, Resource.class))
            .toList();
    }

    public static List<Resource> getInstancesOfClass(Model model, Resource cls) {
        return model.listResourcesWithProperty(RDF.type, cls).toList();
    }

    public static Result<RDFNode> getRequiredObject(Model model, Resource subject, Property predicate) {
        return getObject(model, subject, predicate, CardinalityOne.ONE, RDFNode.class);
    }

    public static Result<Resource> getRequiredResourceObject(Model model, Resource subject, Property predicate) {
        return getObject(model, subject, predicate, CardinalityOne.ONE, Resource.class);
    }

    public static Result<Literal> getRequiredLiteralObject(Model model, Resource subject, Property predicate) {
        return getObject(model, subject, predicate, CardinalityOne.ONE, Literal.class);
    }

    public static Result<RDFList> getRequiredListObject(Model model, Resource subject, Property predicate) {
        return getObject(model, subject, predicate, CardinalityOne.ONE, RDFList.class);
    }

    public static Result<Resource> getRequiredURIResourceObject(Model model, Resource subject, Property predicate) {
        return getObject(model, subject, predicate, CardinalityOne.ONE, Resource.class)
            .flatMap(RDFNodes::castURIResource);
    }

    public static Result<RDFNode> getOptionalObject(Model model, Resource subject, Property predicate) {
        return getObject(model, subject, predicate, CardinalityOne.ZERO_ONE, RDFNode.class);
    }

    public static Result<Resource> getOptionalResourceObject(Model model, Resource subject, Property predicate) {
        return getObject(model, subject, predicate, CardinalityOne.ZERO_ONE, Resource.class);
    }

    public static Result<Literal> getOptionalLiteralObject(Model model, Resource subject, Property predicate) {
        return getObject(model, subject, predicate, CardinalityOne.ZERO_ONE, Literal.class);
    }

    public static <X extends RDFNode> Result<X> getObject(Model model, Resource subject, Property predicate,
                                                          CardinalityOne card, Class<X> type) {

        StmtIterator statements = model.listStatements(subject, predicate, (RDFNode) null);

        // Must be 1 object
        if (!statements.hasNext()
            && (card == CardinalityOne.ONE_MANY || card == CardinalityOne.ONE)) {
            return getErrorResult(model, subject, predicate, "Expected " + card + " object, but got 0.");
        }

        Optional<RDFNode> object = statements.nextOptional().map(Statement::getObject);

        // Cannot be more than one object
        if (object.isPresent() && statements.hasNext()
            && (card == CardinalityOne.ZERO_ONE || card == CardinalityOne.ONE)) {
            int noObjects = statements.toList().size() + 1;
            return getErrorResult(model, subject, predicate, "Expected " + card + " object, but got " + noObjects + " .");
        }

        // Cast to specified type
        return object.isPresent() ? RDFNodes.cast(object.get(), type) : Result.empty();
    }


    private static Result getErrorResult(Model model, Resource subject, Property predicate, String error) {
        return Result.error(
            "Error getting property value for " + RDFNodes.toString(model, predicate)
                + " for subject '" + RDFNodes.toString(model, subject) + "'. " + error);
    }

}


