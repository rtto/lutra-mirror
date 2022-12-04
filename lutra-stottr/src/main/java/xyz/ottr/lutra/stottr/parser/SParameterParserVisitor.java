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

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.model.types.Type;
import xyz.ottr.lutra.parser.ParameterBuilder;
import xyz.ottr.lutra.stottr.STOTTR;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;
import xyz.ottr.lutra.system.Result;

class SParameterParserVisitor extends SBaseParserVisitor<Parameter> {

    private final STypeParserVisitor typeParser;
    private final STermParserVisitor termParser;

    SParameterParserVisitor(STermParserVisitor termParser) {
        this.termParser = termParser;
        this.typeParser = new STypeParserVisitor(termParser);
    }


    public Result<Parameter> visitParameter(stOTTRParser.ParameterContext ctx) {

        var modifiers = parseModifiers(ctx);

        return ParameterBuilder.builder()
            .term(parseTerm(ctx))
            .type(parseType(ctx))
            .name(parseName(ctx))
            .optional(Result.of(modifiers.contains(STOTTR.Parameters.optional)))
            .nonBlank(Result.of(modifiers.contains(STOTTR.Parameters.nonBlank)))
            .defaultValue(parseDefaultValue(ctx))
            .build();
    }

    private Set<String> parseModifiers(stOTTRParser.ParameterContext ctx) {

        if (ctx.ParameterMode() == null) {
            return Collections.emptySet();
        } else {
            return ctx.ParameterMode().stream()
                .map(TerminalNode::getSymbol)
                .map(Token::getText)
                .collect(Collectors.toSet());
        }
    }

    private Result<Term> parseDefaultValue(stOTTRParser.ParameterContext ctx) {

        if (ctx.defaultValue() == null) {
            return Result.empty();
        }

        return this.termParser.visit(ctx.defaultValue().constantTerm());
    }

    private Result<Term> parseTerm(stOTTRParser.ParameterContext ctx) {
        if (ctx.Variable() == null) {
            return Result.error("Unrecognized parameter variable " + SParserUtils.getTextWithLineAndColumnString(ctx));
        }
        return Result.of(new BlankNodeTerm(this.termParser.getVariableLabel(ctx.Variable())));
    }

    private Result<String> parseName(stOTTRParser.ParameterContext ctx) {
        if (ctx.Variable() == null) {
            return Result.error("Unrecognized parameter variable " + SParserUtils.getTextWithLineAndColumnString(ctx));
        }
        return Result.of(this.termParser.getVariableLabel(ctx.Variable()));
    }

    private Result<Type> parseType(stOTTRParser.ParameterContext ctx) {
        return Result.ofNullable(ctx.type())
                .flatMap(this.typeParser::visit);
    }
}

