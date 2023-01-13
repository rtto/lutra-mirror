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
import org.antlr.v4.runtime.Token;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.ListExpander;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.parser.InstanceBuilder;
import xyz.ottr.lutra.stottr.STOTTR;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;
import xyz.ottr.lutra.system.Result;

public class SInstanceParserVisitor extends SBaseParserVisitor<Instance> {

    private STermParserVisitor termParser;
    private SArgumentParserVisitor argumentParser;

    // list of the types of statements this parser accepts. Used in messages when other types are ignored.
    private static final String acceptingList = "instances";

    SInstanceParserVisitor(STermParserVisitor termParser) {
        this.termParser = termParser;
        this.argumentParser = new SArgumentParserVisitor(termParser);
    }

    SInstanceParserVisitor(Map<String, String> prefixes) {
        this(new STermParserVisitor(prefixes));
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

        if (ctx.templateName() == null || ctx.templateName().iri() == null) {
            return Result.error("Unrecognized instance template IRI " + SParserUtils.getTextWithLineAndColumnString(ctx));
        }

        return this.termParser
            .visitIri(ctx.templateName().iri())
            .map(iri -> ((IRITerm) iri).getIri());
    }

    private Result<ListExpander> parseExpander(stOTTRParser.InstanceContext ctx) {

        if (ctx.ListExpander() == null) {
            return Result.empty();
        }

        return Result.ofNullable(ctx.ListExpander().getSymbol())
                .map(Token::getText)
                .map(STOTTR.Expanders.map::getKey)
                .or(() -> Result.error("Unrecognized list expander " + SParserUtils.getTextWithLineAndColumnString(ctx)));
    }

    private Result<List<Argument>> parseArguments(stOTTRParser.InstanceContext ctx) {

        if (ctx.argumentList() == null || ctx.argumentList().argument() == null) {
            return Result.error("Unrecognized instance arguments " + SParserUtils.getTextWithLineAndColumnString(ctx));
        }

        return ctx.argumentList()
            .argument()
            .stream()
            .map(this.argumentParser::visitArgument)
            .collect(Collectors.collectingAndThen(Collectors.toList(), Result::aggregate));
    }

    @Override
    public Result visitBaseTemplate(stOTTRParser.BaseTemplateContext ctx) {
        return SParserUtils.ignoreStatement("base template", acceptingList, ctx);
    }

    @Override
    public Result visitTemplate(stOTTRParser.TemplateContext ctx) {
        return SParserUtils.ignoreStatement("template", acceptingList, ctx);
    }

    @Override
    public Result visitSignature(stOTTRParser.SignatureContext ctx) {
        return SParserUtils.ignoreStatement("signature", acceptingList, ctx);
    }

}
