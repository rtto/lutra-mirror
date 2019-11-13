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
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;

public abstract class SParser<T> extends SBaseParserVisitor<T> {

    private Map<String, String> prefixes = new HashMap<>();
    private STermParser termParser = new STermParser(this.prefixes);

    public Map<String, String> getPrefixes() {
        return Collections.unmodifiableMap(this.prefixes);
    }

    protected void setPrefixesAndVariables(Map<String, String> prefixes, Map<String, Term> variables) {
        this.prefixes = prefixes;
        this.termParser = new STermParser(prefixes, variables);
    }

    /**
     * Should initialize subparsers that depend on
     * the prefix definitions and this.termParser
     * (such as ParameterParsers)
     */
    protected abstract void initSubParsers();

    @Override
    public abstract Result<T> visitStatement(stOTTRParser.StatementContext ctx);

    public ResultStream<T> parseString(String str) {
        return parseDocument(CharStreams.fromString(str));
    }

    protected STermParser getTermParser() {
        return this.termParser;
    }

    protected ResultStream<T> parseDocument(CharStream in) {
        // Make parser
        ErrorToMessageListener errListener = new ErrorToMessageListener();
        stOTTRParser parser = SParserUtils.makeParser(in, errListener);
        stOTTRParser.StOTTRDocContext document = parser.stOTTRDoc();

        // Parse prefixes
        SPrefixParser prefixParser = new SPrefixParser();
        Result<Map<String, String>> prefixRes = prefixParser.visit(document);
        if (!prefixRes.isPresent()) {
            return ResultStream.of(prefixRes.map(obj -> (T) obj));
        }
        this.prefixes.putAll(prefixRes.get());
        this.termParser = new STermParser(this.prefixes);

        initSubParsers();
        
        // Parse instances/templates
        // Below code will not be executed if prefixes are not present
        ResultStream<T> resultStream = prefixRes.mapToStream(_ignore -> {

            Stream<Result<T>> results = document
                .statement() // List of statements
                .stream()
                .map(this::visitStatement);

            return new ResultStream<>(results);
        });

        // TODO: Somehow put some of these messages more fine-grained on returned instances,
        //       see https://gitlab.com/ottr/lutra/lutra/issues/148
        MessageHandler messageHandler = errListener.getMessageHandler();
        Optional<Message> listenerMessage = messageHandler.toSingleMessage("Parsing stOTTR");
        if (listenerMessage.isPresent()) {
            resultStream = ResultStream.concat(ResultStream.of(Result.empty(listenerMessage.get())), resultStream);
        }

        return resultStream;
    }
}
