package xyz.ottr.lutra.wottr.parser.v03;

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

import java.util.Collections;
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
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.model.types.TermType;
import xyz.ottr.lutra.model.types.TypeFactory;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.wottr.parser.TermFactory;
import xyz.ottr.lutra.wottr.parser.v03.util.ModelSelector;
import xyz.ottr.lutra.wottr.parser.v03.util.ModelSelectorException;
import xyz.ottr.lutra.wottr.util.RDFNodes;
import xyz.ottr.lutra.wottr.vocabulary.v03.WOTTR;

public class WParameterParser implements Function<Resource, Result<Term>> {

    private final Model model;
    private final TermFactory rdfTermFactory;
    private final Set<Term> optionals;
    private final Map<Term, Term> defaultValues;



    public WParameterParser(Model model) {
        this.model = model;
        this.rdfTermFactory = new TermFactory(WOTTR.theInstance);
        this.optionals = new HashSet<>();
        this.defaultValues = new HashMap<>();
    }

    public Result<Term> apply(Resource p) {

        Result<Term> resultTerm;

        try {
            Property type;
            
            // Must have a variable/value:
            Statement varAssignment = ModelSelector.getOptionalStatementWithProperties(this.model, p,
                    WOTTR.ALL_variable);
            type = varAssignment != null ? varAssignment.getPredicate() : null;

            resultTerm = varAssignment != null
                ? this.rdfTermFactory.apply(varAssignment.getObject())
                : Result.error("No variable for parameter " + RDFNodes.toString(p) + ".");

            // Set default variable type, as legacy does not have a notion of types
            if (type != null) {
                resultTerm.ifPresent(term -> setType(term, type));
            }

            // Add to optional if necessary
            Optional<Literal> optionalLit = Optional.ofNullable(
                    ModelSelector.getOptionalLiteralOfProperty(this.model, p, WOTTR.optional));
            if (optionalLit.filter(Literal::getBoolean).isPresent()) {
                resultTerm.ifPresent(this.optionals::add);
            }
            // TODO: Check and add default values
        } catch (ModelSelectorException ex) {
            // TODO: Correct lvl and good message?
            resultTerm = Result.error("Error parsing parameter. " + ex.getMessage());
        }

        return resultTerm;
    }

    public Set<Term> getOptionals() {
        return Collections.unmodifiableSet(this.optionals);
    }

    public Map<Term, Term> getDefaultValues() {
        return Collections.unmodifiableMap(this.defaultValues);
    }

    private static final Map<Property, TermType> PROPERTY_TERM_TYPE_MAP;

    static {
        Map<Property, TermType> typeMap = new HashMap<>();
        typeMap.put(WOTTR.literalVariable, TypeFactory.getType(RDFS.Literal));
        typeMap.put(WOTTR.classVariable, TypeFactory.getType(OWL.Class));
        typeMap.put(WOTTR.individualVariable, TypeFactory.getType(OWL.NS + "NamedIndividual"));
        typeMap.put(WOTTR.propertyVariable, TypeFactory.getType(OTTR.TypeURI.IRI));
        typeMap.put(WOTTR.dataPropertyVariable, TypeFactory.getType(OWL.DatatypeProperty));
        typeMap.put(WOTTR.annotationPropertyVariable, TypeFactory.getType(OWL.AnnotationProperty));
        typeMap.put(WOTTR.objectPropertyVariable, TypeFactory.getType(OWL.ObjectProperty));
        typeMap.put(WOTTR.datatypeVariable, TypeFactory.getType(RDFS.Datatype));
        typeMap.put(WOTTR.variable, TypeFactory.getTopType());
        PROPERTY_TERM_TYPE_MAP = Collections.unmodifiableMap(typeMap);
    }

    private void setType(Term term, Property property) {
        term.setType(PROPERTY_TERM_TYPE_MAP.getOrDefault(property, TypeFactory.getVariableType(term)));
    }
}
