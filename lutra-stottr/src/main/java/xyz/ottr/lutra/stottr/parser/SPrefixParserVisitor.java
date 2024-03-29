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

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.stottr.antlr.stOTTRBaseVisitor;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;
import xyz.ottr.lutra.util.PrefixValidator;

public class SPrefixParserVisitor extends SBaseParserVisitor<Map<String, String>> {

    public Result<Map<String, String>> visitStOTTRDoc(stOTTRParser.StOTTRDocContext ctx) {

        if (ctx.directive().isEmpty()) {
            return Result.of(new HashMap<>());
        }

        SDirectiveParser dirParser = new SDirectiveParser();

        return ResultStream.innerOf(ctx.directive())
            .mapFlatMap(dirParser::visit)
            .innerMap(PrefixPair::toMapEntry)
            .aggregate()
            .map(stream -> stream.collect(Collectors.toList()))
            .flatMap(PrefixValidator::buildPrefixMapping)
            .flatMap(PrefixValidator::check)
            .map(PrefixMapping::getNsPrefixMap);
    }

    private static class SDirectiveParser extends stOTTRBaseVisitor<Result<PrefixPair>> {

        public Result<PrefixPair> visitBase(stOTTRParser.BaseContext ctx) {
            return parseBasePrefix(ctx.IRIREF(), ctx);
        }

        public Result<PrefixPair> visitSparqlBase(stOTTRParser.SparqlBaseContext ctx) {
            return parseBasePrefix(ctx.IRIREF(), ctx);
        }

        public Result<PrefixPair> visitPrefixID(stOTTRParser.PrefixIDContext ctx) {
            return parsePrefix(ctx.PNAME_NS(), ctx.IRIREF(), ctx);
        }

        public Result<PrefixPair> visitSparqlPrefix(stOTTRParser.SparqlPrefixContext ctx) {
            return parsePrefix(ctx.PNAME_NS(), ctx.IRIREF(), ctx);
        }

        private Result<PrefixPair> parseBasePrefix(TerminalNode iriref, ParserRuleContext ctx) {
            if (iriref == null) {
                return Result.error("Syntax error in base prefix declaration: unparsable base URI, "
                    + SParserUtils.getTextWithLineAndColumnString(ctx));
            }
            return Result.of(PrefixPair.makeBase(iriref));
        }

        private Result<PrefixPair> parsePrefix(TerminalNode prefixName, TerminalNode iriref, ParserRuleContext ctx) {
            if (prefixName == null || iriref == null) {
                String errorMessage = "Syntax error in prefix declaration:";

                if (prefixName == null) {
                    errorMessage += " unparsable prefix name,";
                }
                if (iriref == null) {
                    errorMessage += " unparsable prefix URI,";
                }
                return Result.error(errorMessage + SParserUtils.getTextWithLineAndColumnString(ctx));
            }

            return Result.of(PrefixPair.makePrefix(prefixName, iriref));
        }
    }

    private static class PrefixPair {

        private static final String BASE_PREFIX = ""; // TODO: correct rep. of (empty) base?
        private static final Pattern lastColonPat = Pattern.compile(":$");
        private static final Pattern angularBracesPat = Pattern.compile("^<|>$");

        public final String prefixName;
        public final String prefixURI;

        PrefixPair(String prefixName, String prefixURI) {
            this.prefixName = lastColonPat.matcher(prefixName).replaceAll("");
            this.prefixURI = angularBracesPat.matcher(prefixURI).replaceAll("");
        }

        static PrefixPair makeBase(TerminalNode baseNode) {
            String baseURI = baseNode.getSymbol().getText();
            return new PrefixPair(BASE_PREFIX, baseURI);
        }

        static PrefixPair makePrefix(TerminalNode prefixNode, TerminalNode nsNode) {
            String prefix = prefixNode.getSymbol().getText();
            String ns = nsNode.getSymbol().getText();
            return new PrefixPair(prefix, ns);
        }

        private Map.Entry<String, String> toMapEntry() {
            return new AbstractMap.SimpleEntry<>(prefixName, prefixURI);
        }
    }
}
