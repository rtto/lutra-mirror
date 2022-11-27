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
import java.util.Map;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.tree.TerminalNode;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.ListExpander;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.parser.InstanceBuilder;
import xyz.ottr.lutra.parser.InstanceParser;
import xyz.ottr.lutra.stottr.STOTTR;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;
import xyz.ottr.lutra.system.Result;

public class SInstanceParser extends SDocumentParser<Instance> implements InstanceParser<CharStream> {

    protected STermParser termParser;
    protected SArgumentParser argumentParser;

    public SInstanceParser() {
    }

    SInstanceParser(Map<String, String> prefixes, STermParser termParser) {
        setPrefixes(prefixes);
        this.termParser = termParser;
        this.argumentParser = new SArgumentParser(termParser);
    }

    SInstanceParser(Map<String, String> prefixes) {
        this(prefixes, new STermParser(prefixes));
    }

    @Override
    void initSubParsers() {
        this.termParser = new STermParser(getPrefixes());
        this.argumentParser = new SArgumentParser(this.termParser);
    }

    @Override
    public Result<Instance> visitInstance(stOTTRParser.InstanceContext ctx) {
        return InstanceBuilder.builder()
            .iri(parseIRI(ctx))
            .listExpander(parseExpander(ctx))
            .arguments(parseArguments(ctx))
            .build();
    }

    private Result<String> parseIRI(stOTTRParser.InstanceContext ctx) {
        return this.termParser
            .visitIri(ctx.templateName().iri())
            .map(iri -> ((IRITerm) iri).getIri());
    }

    private Result<ListExpander> parseExpander(stOTTRParser.InstanceContext ctx) {

        TerminalNode expanderNode = ctx.ListExpander();

        if (expanderNode == null) {
            return Result.empty();
        }

        String expanderValue = expanderNode.getSymbol().getText();

        return STOTTR.Expanders.map.containsValue(expanderValue)
            ? Result.of(STOTTR.Expanders.map.getKey(expanderValue))
            : Result.error("Unrecognized list expander, " + SParserUtils.getTextWithLineAndColumnString(ctx));
    }

    private Result<List<Argument>> parseArguments(stOTTRParser.InstanceContext ctx) {
        return ctx.argumentList()
            .argument()
            .stream()
            .map(arg -> this.argumentParser.visitArgument(arg))
            .collect(Collectors.collectingAndThen(Collectors.toList(), Result::aggregate));
    }


}
