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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.tree.TerminalNode;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.stottr.STOTTR;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;
import xyz.ottr.lutra.system.Result;

public class SArgumentListParser extends SBaseParserVisitor<ArgumentList> {

    private final STermParser termParser;

    /**
     * @param termParser
     *     The parser to use for parsing terms in argument list, which should contain
     *     the prefix definitions of this context.
     */
    public SArgumentListParser(STermParser termParser) {
        this.termParser = termParser;
    }

    /**
     * Entry-point of parsing set to InstanceContext as we need to have
     * the expander, which is set on instance and not on argument list itself.
     */
    @Override
    public Result<ArgumentList> visitInstance(stOTTRParser.InstanceContext ctx) {

        return parseArguments(ctx.argumentList())
            .flatMap(argLst -> {

                // Get terms
                List<Term> terms = argLst.stream()
                    .map(arg -> arg.term)
                    .collect(Collectors.toList());

                // Find terms with list expander
                Set<Term> expanderValues = argLst.stream()
                    .filter(arg -> arg.expander)
                    .map(arg -> arg.term)
                    .collect(Collectors.toSet());

                // Parse potential expander
                TerminalNode expanderNode = ctx.ListExpander();
                if (expanderNode != null) {
                    Result<ArgumentList.Expander> expRes = parseExpander(expanderNode.getSymbol().getText());
                    return expRes.map(expander -> new ArgumentList(terms, expanderValues, expander));
                } else {
                    return Result.of(new ArgumentList(terms));
                }
            }
        );
    }

    protected Result<ArgumentList.Expander> parseExpander(String expanderStr) {
        return STOTTR.Expanders.map.containsKey(expanderStr)
            ? Result.of(STOTTR.Expanders.map.get(expanderStr))
            : Result.error("Unrecognized list expander: " + expanderStr);
    }
    
    private Result<List<Argument>> parseArguments(stOTTRParser.ArgumentListContext ctx) {
        
        List<Result<Argument>> termsResList = ctx.argument()
            .stream()
            .map(this::parseArgument)
            .collect(Collectors.toList());

        return Result.aggregate(termsResList);
    }

    private Result<Argument> parseArgument(stOTTRParser.ArgumentContext ctx) {

        Result<Term> termRes = this.termParser.visitTerm(ctx.term());
        boolean expander = ctx.ListExpand() != null;
        
        return termRes.map(term -> new Argument(term, expander));
    }
    
    private static class Argument {

        public final Term term;
        public final Boolean expander;

        public Argument(Term term, Boolean expander) {
            this.term = term;
            this.expander = expander;
        }
    }
}
