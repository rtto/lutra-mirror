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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;

public abstract class SDocumentParser<T> extends SBaseParserVisitor<T> {

    //private static final int messageDigestMaxLength = 30;

    protected Map<String, String> prefixes = new HashMap<>();

    public Map<String, String> getPrefixes() {
        return Collections.unmodifiableMap(this.prefixes);
    }

    abstract void initSubParsers();

    private void initPrefixes(Map<String, String> prefixes) {
        // TODO should check for prefix conflicts
        this.prefixes.putAll(prefixes);
    }

    public ResultStream<T> apply(CharStream in) {
        return parseDocument(in);
    }

    public ResultStream<T> parseString(String str) {
        return parseDocument(CharStreams.fromString(str));
    }

    private Result<Map<String, String>> parsePrefixes(stOTTRParser.StOTTRDocContext ctx) {
        var prefixParser = new SPrefixParser();
        return prefixParser.visit(ctx);
    }

    private ResultStream<T> parseStatements(stOTTRParser.StOTTRDocContext ctx) {

        var parsedStatements = ctx
                .statement() // List of statements
                .stream()
                .map(this::visitStatement);

        return new ResultStream<>(parsedStatements);
    }

    private ResultStream<T> parseDocument(CharStream in) {
        // Make parser
        ErrorToMessageListener errListener = new ErrorToMessageListener();
        stOTTRParser parser = SParserUtils.makeParser(in, errListener);

        stOTTRParser.StOTTRDocContext document = parser.stOTTRDoc();

        var prefixes = parsePrefixes(document);
        var statements = prefixes.mapToStream(pxs -> {
            initPrefixes(pxs);
            initSubParsers();
            return parseStatements(document);
        });


        // TODO: Somehow put some of these messages more fine-grained on returned instances,
        //       see https://gitlab.com/ottr/lutra/lutra/issues/148
        MessageHandler messageHandler = errListener.getMessageHandler();
        Optional<Message> listenerMessage = messageHandler.toSingleMessage("Parsing stOTTR");
        if (listenerMessage.isPresent()) {
            statements = ResultStream.concat(ResultStream.of(Result.empty(listenerMessage.get())), statements);
        }

        return statements;
    }

    /*
    // These visit methods must be overwritten in extending classes.

    public Result visitBaseTemplate(stOTTRParser.BaseTemplateContext ctx) {
        return ignoreStatement("base template", ctx);
    }

    public Result visitTemplate(stOTTRParser.TemplateContext ctx) {
        return ignoreStatement("template", ctx);
    }

    public Result visitSignature(stOTTRParser.SignatureContext ctx) {
        return ignoreStatement("signature", ctx);
    }

    public Result visitInstance(stOTTRParser.InstanceContext ctx) {
        return ignoreStatement("instance", ctx);
    }

    private static Result ignoreStatement(String name, ParserRuleContext ctx) {
        return Result.info("Ignoring statement '" + name + "': " + StringUtils.truncate(ctx.getText(), messageDigestMaxLength));
    }
    */

}
