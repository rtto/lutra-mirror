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
import java.util.Map;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.tree.TerminalNode;

import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.stottr.antlr.stOTTRBaseVisitor;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;

public class SPrefixParser extends SBaseParserVisitor<Map<String, String>> {

    @Override
    public Result<Map<String, String>> visitStOTTRDoc(stOTTRParser.StOTTRDocContext ctx) {

        if (ctx.directive().isEmpty()) {
            return Result.of(new HashMap<>());
        }

        SDirectiveParser dirParser = new SDirectiveParser();

        return ResultStream.innerOf(ctx.directive())
            .innerMap(dirParser::visit)
            .aggregate()
            .flatMap(pfs -> {
                Map<String, String> m = new HashMap<>();
                pfs.forEach(pair -> m.put(pair.ns, pair.prefix)); // TODO: Check for collitions
                // TODO: Check if ns-prefix-pair is non-standard combination and give error
                return Result.of(m);
            });
    }

    private static class SDirectiveParser extends stOTTRBaseVisitor<PrefixPair> {

        @Override
        public PrefixPair visitPrefixID(stOTTRParser.PrefixIDContext ctx) {
            return PrefixPair.makePrefix(ctx.PNAME_NS(), ctx.IRIREF());
        }

        @Override
        public PrefixPair visitBase(stOTTRParser.BaseContext ctx) {
            return PrefixPair.makeBase(ctx.IRIREF());
        }

        @Override
        public PrefixPair visitSparqlBase(stOTTRParser.SparqlBaseContext ctx) {
            return PrefixPair.makeBase(ctx.IRIREF());
        }

        @Override
        public PrefixPair visitSparqlPrefix(stOTTRParser.SparqlPrefixContext ctx) {
            return PrefixPair.makePrefix(ctx.PNAME_NS(), ctx.IRIREF());
        }
    }

    private static class PrefixPair {

        private static final String BASE_PREFIX = ""; // TODO: correct rep. of (empty) base?
        private static final Pattern colonPat = Pattern.compile(":$");
        private static final Pattern angularPat = Pattern.compile("^<|>$");

        public final String ns;
        public final String prefix;

        public PrefixPair(String ns, String prefix) {
            this.ns = stripNamespace(ns);
            this.prefix = stripPrefix(prefix);
        }

        private static String stripNamespace(String ns) {
            return colonPat.matcher(ns).replaceAll("");
        }

        private static String stripPrefix(String prefix) {
            return angularPat.matcher(prefix).replaceAll("");
        }

        public static PrefixPair makeBase(TerminalNode prefixNode) {
            String prefix = prefixNode.getSymbol().getText();
            return new PrefixPair(BASE_PREFIX, prefix);
        }

        public static PrefixPair makePrefix(TerminalNode nsNode, TerminalNode prefixNode) {
            String ns = nsNode.getSymbol().getText();
            String prefix = prefixNode.getSymbol().getText();
            return new PrefixPair(ns, prefix);
        }
    }
}
