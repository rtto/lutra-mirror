package xyz.ottr.lutra.wottr.parser.v04;

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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.model.types.TermType;
import xyz.ottr.lutra.model.types.TypeFactory;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.wottr.parser.TermFactory;
import xyz.ottr.lutra.wottr.parser.TermTypeFactory;
import xyz.ottr.lutra.wottr.util.ModelSelector;
import xyz.ottr.lutra.wottr.util.RDFNodes;
import xyz.ottr.lutra.wottr.vocabulary.v04.WOTTR;

public class WParameterParser implements Function<RDFNode, Result<Term>> {

    private final Model model;
    private final TermFactory rdfTermFactory;
    private final TermTypeFactory typeFactory;
    private final Set<Term> optionals;
    private final Set<Term> nonBlanks;
    private final Map<Term, Term> defaultValues;

    public WParameterParser(Model model) {
        this.model = model;
        this.rdfTermFactory = new TermFactory(WOTTR.theInstance);
        this.typeFactory = new TermTypeFactory();
        this.optionals = new HashSet<>();
        this.nonBlanks = new HashSet<>();
        this.defaultValues = new HashMap<>();
    }

    public Result<Term> apply(RDFNode paramNode) {

        Result<Resource> parameter = RDFNodes.cast(paramNode, Resource.class);

        Result<Term> term = parameter.flatMap(this::getParameterTerm);

        // get and set term type
        Result<TermType> termtype = Result.apply(parameter, term, this::getParameterType);
        term.addResult(termtype, Term::setType);

        // get and set modifiers
        Result<List<Resource>> modifiers = parameter.flatMap(this::getModifiers);
        term.addResult(modifiers, this::setModifiers);

        // get and set default value
        Result<Term> defaultValue = parameter.flatMap(this::getDefaultValue);
        term.addResult(defaultValue, this.defaultValues::put);

        return term;
    }

    private Result<Term> getParameterTerm(Resource parameter) {
        return ModelSelector.getRequiredObject(this.model, parameter, WOTTR.variable)
            .flatMap(this.rdfTermFactory);
    }

    private Result<TermType> getParameterType(Resource parameter, Term term) {
        Result<TermType> type = ModelSelector.getOptionalResourceObject(this.model, parameter, WOTTR.type)
            .flatMap(this.typeFactory);
        return Result.of(type.orElse(TypeFactory.getVariableType(term)), type);
    }

    private Result<List<Resource>> getModifiers(Resource parameter) {

        List<Result<Resource>> modifiers = ResultStream
            .of(ModelSelector.getResourceObjects(this.model, parameter, WOTTR.modifier))
            .mapFlatMap(r -> WOTTR.argumentModifiers.contains(r)
                ? Result.of(r)
                : Result.error("Unknown modifier " + RDFNodes.toString(r) + " in parameter " + RDFNodes.toString(parameter) + "."))
            .collect(Collectors.toList());

        return Result.aggregate(modifiers);
    }

    private void setModifiers(Term term, List<Resource> modifiers) {

        for (Resource modifier : modifiers) {
            if (modifier.equals(WOTTR.optional)) {
                this.optionals.add(term);
            } else if (modifier.equals(WOTTR.nonBlank)) {
                this.nonBlanks.add(term);
            }
        }
    }

    private Result<Term> getDefaultValue(Resource param) {
        return ModelSelector.getOptionalObject(this.model, param, WOTTR.defaultVal)
            .flatMap(this.rdfTermFactory);
    }

    public Set<Term> getOptionals() {
        return Collections.unmodifiableSet(this.optionals);
    }

    public Set<Term> getNonBlanks() {
        return Collections.unmodifiableSet(this.nonBlanks);
    }

    public Map<Term, Term> getDefaultValues() {
        return Collections.unmodifiableMap(this.defaultValues);
    }
}
