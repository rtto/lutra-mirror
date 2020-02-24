package xyz.ottr.lutra.parser;

/*-
 * #%L
 * lutra-core
 * %%
 * Copyright (C) 2018 - 2020 University of Oslo
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

import lombok.Builder;

import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.system.Result;

public class ParameterParser {

    @Builder
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static Result<Parameter> createParameter(Result<Term> term, Result<Boolean> nonBlank,
        Result<Boolean> optional, Result<Term> defaultValue) {

        nonBlank = Result.nullToEmpty(nonBlank);
        optional = Result.nullToEmpty(optional);
        defaultValue = Result.nullToEmpty(defaultValue);

        var builder = Result.of(Parameter.builder());
        builder.addResult(term, Parameter.ParameterBuilder::term);
        builder.addResult(nonBlank, Parameter.ParameterBuilder::nonBlank);
        builder.addResult(optional, Parameter.ParameterBuilder::optional);
        builder.addResult(defaultValue, Parameter.ParameterBuilder::defaultValue);
        var parameter = builder.map(Parameter.ParameterBuilder::build);

        return parameter;
    }
}
