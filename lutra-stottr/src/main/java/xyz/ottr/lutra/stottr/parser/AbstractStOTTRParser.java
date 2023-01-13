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
import java.util.function.Function;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;

public abstract class AbstractStOTTRParser<T> implements Function<CharStream, ResultStream<T>> {

    private Map<String, String> prefixes;
    private Function<Map<String, String>, SBaseParserVisitor<T>> statementParserProvider;

    /**
     * @param parserVisitor function to produce a parserVisitor (typically a STemplateParserVisitor
     *                      or SInstanceParserVisitor) given a prefix map.
     */
    protected AbstractStOTTRParser(Function<Map<String, String>, SBaseParserVisitor<T>> parserVisitor) {
        this.prefixes  = new HashMap<>();
        this.statementParserProvider = parserVisitor;
    }

    public Map<String, String> getPrefixes() {
        return Collections.unmodifiableMap(this.prefixes);
    }

    public ResultStream<T> apply(CharStream in) {
        return parseDocument(in);
    }

    public ResultStream<T> apply(String str) {
        return parseDocument(CharStreams.fromString(str));
    }

    private Result<Map<String, String>> parsePrefixes(stOTTRParser.StOTTRDocContext ctx) {
        var prefixParser = new SPrefixParserVisitor();
        return prefixParser.visit(ctx);
    }

    private ResultStream<T> parseStatements(stOTTRParser.StOTTRDocContext ctx, Map<String, String> prefixes) {
        var parser = this.statementParserProvider.apply(prefixes);

        var parsedStatements = ctx
                .statement() // List of statements
                .stream()
                .map(parser::visitStatement);

        return new ResultStream<T>(parsedStatements);
    }

    private ResultStream<T> parseDocument(CharStream in) {
        ErrorToMessageListener errListener = new ErrorToMessageListener();
        stOTTRParser parser = SParserUtils.makeParser(in, errListener);

        stOTTRParser.StOTTRDocContext document = parser.stOTTRDoc();

        var prefixes = parsePrefixes(document);
        prefixes.ifPresent(this.prefixes::putAll);

        ResultStream<T> statements = parsePrefixes(document)
                .mapToStream(pxs -> this.parseStatements(document, pxs));

        // TODO: Somehow put some of these messages more fine-grained on returned instances,
        //       see https://gitlab.com/ottr/lutra/lutra/issues/148
        MessageHandler messageHandler = errListener.getMessageHandler();
        Optional<Message> listenerMessage = messageHandler.toSingleMessage("Parsing stOTTR");
        if (listenerMessage.isPresent()) {
            statements = ResultStream.concat(ResultStream.of(Result.empty(listenerMessage.get())), statements);
        }

        return statements;
    }

}
