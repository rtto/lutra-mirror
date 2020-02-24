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

import java.util.function.Function;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.parser.ParameterParser;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.wottr.parser.TermFactory;
import xyz.ottr.lutra.wottr.parser.TermTypeFactory;
import xyz.ottr.lutra.wottr.util.ModelSelector;
import xyz.ottr.lutra.wottr.util.RDFNodes;
import xyz.ottr.lutra.wottr.WOTTR;

public class WParameterParser extends ParameterParser implements Function<RDFNode, Result<Parameter>> {

    private final Model model;
    private final TermFactory rdfTermFactory;
    private final TermTypeFactory typeFactory;

    WParameterParser(Model model) {
        this.model = model;
        this.rdfTermFactory = new TermFactory();
        this.typeFactory = new TermTypeFactory();
    }

    public Result<Parameter> apply(RDFNode paramNode) {

        var parameter = RDFNodes.cast(paramNode, Resource.class);

        return builder()
            .term(parameter.flatMap(this::getTerm))
            .optional(parameter.flatMap(this::getOptional))
            .nonBlank(parameter.flatMap(this::getNonBlank))
            .defaultValue(parameter.flatMap(this::getDefaultValue))
            .build();
    }

    private Result<Term> getTerm(Resource parameter) {
        var term = ModelSelector.getRequiredObject(this.model, parameter, WOTTR.variable)
            .flatMap(this.rdfTermFactory);

        var type = ModelSelector.getOptionalResourceObject(this.model, parameter, WOTTR.type)
            .flatMap(this.typeFactory);

        term.addResult(type, Term::setType);
        // TODO: do we need to set type to term.getVariableType() if no type is specified?

        return term;
    }

    private Result<Term> getDefaultValue(Resource param) {
        return ModelSelector.getOptionalObject(this.model, param, WOTTR.defaultVal)
            .flatMap(this.rdfTermFactory);
    }

    private Result<Boolean> getOptional(Resource parameter) {
        return Result.of(this.model.contains(parameter, WOTTR.modifier, WOTTR.optional));
    }

    private Result<Boolean> getNonBlank(Resource parameter) {
        return Result.of(this.model.contains(parameter, WOTTR.modifier, WOTTR.nonBlank));
    }

    // TODO check if there are other modifiers than optional and nonBlank, or rather use SHACL?
}