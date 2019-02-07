package xyz.ottr.lutra.wottr.legacy.io;

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.model.types.TypeFactory;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.wottr.legacy.WOTTR;
import xyz.ottr.lutra.wottr.legacy.WTermFactory;
import xyz.ottr.lutra.wottr.util.ModelSelector;
import xyz.ottr.lutra.wottr.util.ModelSelectorException;

public class WParameterParser implements Function<Resource, Result<Term>> {

    private final Model model;
    private final WTermFactory rdfTermFactory;
    private final Set<Term> optionals;
    private final Map<Term, Term> defaultValues;

    public WParameterParser(Model model) {
        this.model = model;
        this.rdfTermFactory = new WTermFactory();
        this.optionals = new HashSet<>();
        this.defaultValues = new HashMap<>();
    }

    public Result<Term> apply(Resource p) {

        Result<Term> resultTerm;

        try {
            Property type;
            
            // Must have a variable/value:
            Statement varAssignment = ModelSelector.getOptionalStatementWithProperties(model, p,
                    WOTTR.ALL_variable);
            type = varAssignment != null ? varAssignment.getPredicate() : null;

            resultTerm = varAssignment != null
                ? rdfTermFactory.apply(varAssignment.getObject())
                : Result.empty(new Message(Message.ERROR, "No variable for parameter " + p.toString() + "."));

            // Set default variable type, as legacy does not have a notion of types
            if (type != null) {
                resultTerm.ifPresent(term -> setType(term, type));
            }

            // Add to optional if necessary
            Optional<Literal> optionalLit = Optional.ofNullable(
                    ModelSelector.getOptionalLiteralOfProperty(model, p, WOTTR.optional));
            if (optionalLit.filter(lit -> lit.getBoolean()).isPresent()) {
                resultTerm.ifPresent(term -> optionals.add(term));
            }
            // TODO: Check and add default values
        } catch (ModelSelectorException ex) {
            // TODO: Correct lvl and good message?
            resultTerm = Result.empty(new Message(Message.ERROR, "Error parsing parameter. " + ex.getMessage()));
        }

        return resultTerm;
    }

    public Set<Term> getOptionals() {
        return optionals;
    }

    public Map<Term, Term> getDefaultValues() {
        return this.defaultValues;
    }

    private void setType(Term term, Property type) {
        if (type.equals(WOTTR.literalVariable)) {
            return; // Already set by its xsd datatype or its java class
        } else if (type.equals(WOTTR.classVariable)) {
            term.setType(TypeFactory.getByName("class"));
        } else if (type.equals(WOTTR.individualVariable)) {
            term.setType(TypeFactory.getByName("namedindividual"));
        } else if (type.equals(WOTTR.propertyVariable)) { 
            term.setType(TypeFactory.getByName("property"));
        } else if (type.equals(WOTTR.dataPropertyVariable)) {
            term.setType(TypeFactory.getByName("datatypeproperty"));
        } else if (type.equals(WOTTR.annotationPropertyVariable)) {
            term.setType(TypeFactory.getByName("annotationproperty"));
        } else if (type.equals(WOTTR.objectPropertyVariable)) {
            term.setType(TypeFactory.getByName("objectproperty"));
        } else if (type.equals(WOTTR.datatypeVariable)) {
            term.setType(TypeFactory.getByName("datatype"));
        } else if (type.equals(WOTTR.variable)) {
            term.setType(TypeFactory.getTopType());
        } else {
            term.setType(TypeFactory.getVariableType(term));
        }

    }
}
