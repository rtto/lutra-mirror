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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.tree.TerminalNode;

import xyz.ottr.lutra.model.BlankNodeTerm;
import xyz.ottr.lutra.model.ParameterList;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.model.types.TermType;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.stottr.STOTTR;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;

public class SParameterListParser extends SBaseParserVisitor<ParameterList> {

    private STypeParser typeParser;
    private STermParser termParser;

    public SParameterListParser(STermParser termParser) {
        this.termParser = termParser;
        this.typeParser = new STypeParser(termParser);
    }

    public Result<ParameterList> visitParameterList(stOTTRParser.ParameterListContext ctx) {

        // Need to make a fresh ParameterParser per parameter list to parse
        ParameterParser paramParser = new ParameterParser();
        ctx.parameter().forEach(parCtx -> paramParser.parseParameter(parCtx));
        return paramParser.makeParameterList();
    }

    private class ParameterParser {

        // State necessary to maintain while parsing parameters
        // in order to construct final ParameterList
        private final Set<Term> optionals = new HashSet<>();
        private final Set<Term> nonBlanks = new HashSet<>();
        private final Map<Term, Term> defaults = new HashMap<>();
        private final List<Result<Term>> resParams = new LinkedList<>();
        
        public Result<ParameterList> makeParameterList() {

            Result<List<Term>> paramsRes = Result.aggregate(this.resParams);
            return paramsRes.map(params ->
                new ParameterList(params, this.nonBlanks, this.optionals, this.defaults));
        }
    
        public void parseParameter(stOTTRParser.ParameterContext ctx) {

            Result<Term> defaultRes = parseDefaultValue(ctx.defaultValue());
            Result<TermType> typeRes = parseType(ctx.type());
            Result<Term> varRes = Result.zipNullables(typeRes, defaultRes, (type, deflt) -> {

                    Term var = new BlankNodeTerm(termParser.getVariableLabel(ctx.Variable()));
                    var.setIsVariable(true);
                    if (type != null) {
                        var.setType(type);
                    }
                    if (deflt != null) {
                        this.defaults.put(var, deflt);
                    }
                    return var;
                }
            );
            varRes.ifPresent(var -> parseParameterModes(ctx.ParameterMode(), var));
            resParams.add(varRes);
        }

        private void parseParameterModes(List<TerminalNode> modes, Term var) {

            if (modes == null) {
                return;
            }
            for (TerminalNode mode : modes) {
                if (mode.getSymbol().getText().equals(STOTTR.Parameters.optional)) {
                    this.optionals.add(var);
                } else if (mode.getSymbol().getText().equals(STOTTR.Parameters.nonBlank)) {
                    this.nonBlanks.add(var);
                }
            }
        }

        private Result<Term> parseDefaultValue(stOTTRParser.DefaultValueContext ctx) {
            if (ctx == null) {
                return Result.empty();
            }
            return termParser.visit(ctx.constant());
        }

        private Result<TermType> parseType(stOTTRParser.TypeContext ctx) {
            if (ctx == null) {
                return Result.empty();
            }
            return typeParser.visit(ctx);
        }
    }
}
