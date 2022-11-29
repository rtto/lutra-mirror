package xyz.ottr.lutra.stottr.parser;

/*-
 * #%L
 * lutra-stottr
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

import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.parser.ArgumentBuilder;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;
import xyz.ottr.lutra.system.Result;

class SArgumentParserVisitor extends SBaseParserVisitor<Argument> {

    private final STermParserVisitor termParser;

    /**
     * @param termParser
     *     The parser to use for parsing terms in argument list, which should contain
     *     the prefix definitions of this context.
     */
    SArgumentParserVisitor(STermParserVisitor termParser) {
        this.termParser = termParser;
    }

    public Result<Argument> visitArgument(stOTTRParser.ArgumentContext ctx) {
        return ArgumentBuilder.builder()
            .term(parseTerm(ctx))
            .listExpander(parseListExpander(ctx))
            .build();
    }

    private Result<Term> parseTerm(stOTTRParser.ArgumentContext ctx) {
        if (ctx.term() == null) {
            return Result.error("Unrecognized instance argument " + SParserUtils.getTextWithLineAndColumnString(ctx));
        }

        return this.termParser.visitTerm(ctx.term());
    }

    private Result<Boolean> parseListExpander(stOTTRParser.ArgumentContext ctx) {
        return Result.of(ctx.ListExpand() != null);
    }

}
