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
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;

public enum ArgumentBuilder {
    ;

    @Builder
    public static Result<Argument> createArgument(Result<Term> term, Result<Boolean> listExpander) {

        term = Result.nullToEmpty(term, Message.error("Missing value. An argument must have a term value."));
        listExpander = Result.nullToEmpty(listExpander);

        var builder = Result.of(Argument.builder());
        builder.addResult(term, Argument.ArgumentBuilder::term);
        builder.addResult(listExpander, Argument.ArgumentBuilder::listExpander);

        if (Result.allIsPresent(term)) {
            var argument = builder.map(Argument.ArgumentBuilder::build);
            validate(argument);
            return argument;
        } else {
            return Result.empty(builder);
        }
    }

    private static void validate(Result<Argument> argument) {
        checkValue(argument);
    }

    // Warning if value is a URI in the ottr namespace.
    private static void checkValue(Result<Argument> argument) {
        argument.ifPresent(arg -> {
            var term = arg.getTerm();
            if (term instanceof IRITerm && ((IRITerm) term).getIri().startsWith(OTTR.namespace)) {
                argument.addWarning("Suspicious argument value: " + term
                    + ". The value is in the ottr namespace: " + OTTR.namespace);
            }
        });
    }

}