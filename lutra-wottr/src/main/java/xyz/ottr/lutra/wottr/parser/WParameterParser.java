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
import java.util.Set;
import java.util.function.Function;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.model.types.TermType;
import xyz.ottr.lutra.parser.ParameterBuilder;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.wottr.WOTTR;
import xyz.ottr.lutra.wottr.util.ModelSelector;
import xyz.ottr.lutra.wottr.util.RDFNodes;

public class WParameterParser implements Function<RDFNode, Result<Parameter>> {

    private final Model model;
    private final TermFactory rdfTermFactory;
    private final TermTypeFactory typeFactory;

    WParameterParser(Model model) {
        this.model = model;
        this.rdfTermFactory = new TermFactory();
        this.typeFactory = new TermTypeFactory();
    }

    public Result<Parameter> apply(RDFNode paramNode) {

        var parameterResource = RDFNodes.cast(paramNode, Resource.class);

        var modifiers = parameterResource.map(this::getModifiers);

        var parameter = ParameterBuilder.builder()
            .term(parameterResource.flatMap(this::getTerm))
            .type(parameterResource.flatMap(this::getType))
            .optional(modifiers.map(mods -> mods.contains(WOTTR.optional)))
            .nonBlank(modifiers.map(mods -> mods.contains(WOTTR.nonBlank)))
            .defaultValue(parameterResource.flatMap(this::getDefaultValue))
            .build();

        modifiers.ifPresent(mods -> {
            var modifierCopy = new ArrayList<>(mods); // make a copy in case we want to make use of modifiers later.
            modifierCopy.removeAll(WOTTR.argumentModifiers);
            if (!modifierCopy.isEmpty()) {
                parameter.addError("Unknown modifier. Permissible modifiers are "
                    + RDFNodes.toString(WOTTR.argumentModifiers) + ", but found "
                    + RDFNodes.toString(modifierCopy));
            }
        });

        return parameter;
    }

    private Result<Term> getTerm(Resource parameter) {
        return ModelSelector.getRequiredObject(this.model, parameter, WOTTR.variable)
            .flatMap(this.rdfTermFactory);
    }

    private Result<TermType> getType(Resource parameter) {
        return ModelSelector.getOptionalResourceObject(this.model, parameter, WOTTR.type)
            .flatMap(this.typeFactory);
    }

    private Result<Term> getDefaultValue(Resource param) {
        return ModelSelector.getOptionalObject(this.model, param, WOTTR.defaultVal)
            .flatMap(this.rdfTermFactory);
    }

    private Set<RDFNode> getModifiers(Resource parameter) {
        return this.model.listObjectsOfProperty(parameter, WOTTR.modifier).toSet();
    }

}