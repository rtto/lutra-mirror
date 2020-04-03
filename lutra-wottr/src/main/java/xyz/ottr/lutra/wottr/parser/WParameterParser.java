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
import xyz.ottr.lutra.model.types.Type;
import xyz.ottr.lutra.parser.ParameterBuilder;
import xyz.ottr.lutra.parser.TermParser;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.wottr.WOTTR;
import xyz.ottr.lutra.wottr.util.RDFNodes;
import xyz.ottr.lutra.writer.RDFNodeWriter;

public class WParameterParser implements Function<RDFNode, Result<Parameter>> {

    private final Model model;
    private final TermParser termParser;
    private final WTypeParser typeFactory;

    WParameterParser(Model model) {
        this.model = model;
        this.termParser = new TermParser();
        this.typeFactory = new WTypeParser();
    }

    public Result<Parameter> apply(RDFNode paramNode) {

        var parameterResource = RDFNodes.cast(paramNode, Resource.class);

        var modifiers = parameterResource.map(this::parseModifiers);

        var parameter = ParameterBuilder.builder()
            .term(parameterResource.flatMap(this::parseTerm))
            .type(parameterResource.flatMap(this::parseType))
            .optional(modifiers.map(mods -> mods.contains(WOTTR.optional)))
            .nonBlank(modifiers.map(mods -> mods.contains(WOTTR.nonBlank)))
            .defaultValue(parameterResource.flatMap(this::parseDefaultValue))
            .build();

        checkUnexpectedModifiers(parameter, modifiers);

        return parameter;
    }

    private Result<Term> parseTerm(Resource parameter) {
        return ModelSelector.getRequiredObject(this.model, parameter, WOTTR.variable)
            .flatMap(this.termParser::term);
    }

    private Result<Type> parseType(Resource parameter) {
        return ModelSelector.getOptionalResourceObject(this.model, parameter, WOTTR.type)
            .flatMap(this.typeFactory);
    }

    private Result<Term> parseDefaultValue(Resource param) {
        return ModelSelector.getOptionalObject(this.model, param, WOTTR.defaultVal)
            .flatMap(this.termParser::term);
    }

    private Set<RDFNode> parseModifiers(Resource parameter) {
        return this.model.listObjectsOfProperty(parameter, WOTTR.modifier).toSet();
    }

    private void checkUnexpectedModifiers(Result<Parameter> parameter, Result<Set<RDFNode>> modifiers) {
        modifiers.ifPresent(mods -> {
            var modifierCopy = new ArrayList<>(mods); // make a copy so we don't change input.
            modifierCopy.removeAll(WOTTR.argumentModifiers);
            if (!modifierCopy.isEmpty()) {
                parameter.addError("Unknown parameter modifier: " + RDFNodeWriter.toString(modifierCopy)
                    + " Permissible modifiers are " + RDFNodeWriter.toString(WOTTR.argumentModifiers) + ".");
            }
        });

    }

}