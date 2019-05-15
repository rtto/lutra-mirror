package xyz.ottr.lutra.stottr.io;

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
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import xyz.ottr.lutra.io.InstanceParser;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.IRITerm;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.stottr.antlr.stOTTRBaseVisitor;
import xyz.ottr.lutra.stottr.antlr.stOTTRLexer;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;

public class SInstanceParser extends stOTTRBaseVisitor<Result<Instance>> implements InstanceParser<CharStream> {

    private Map<String, String> prefixes = new HashMap<>();
    private STermParser termParser = new STermParser(prefixes);

    public ResultStream<Instance> parseString(String str) {
        return apply(CharStreams.fromString(str));
    }

    private stOTTRLexer makeLexer(CharStream in, ErrorToMessageListener errListener) {

        stOTTRLexer lexer = new stOTTRLexer(in);
        // Only use our own ErrorListener
        lexer.removeErrorListeners();
        lexer.addErrorListener(errListener);
        return lexer;
    }

    
    private stOTTRParser makeParser(CharStream in, ErrorToMessageListener errListener) {

        stOTTRLexer lexer = makeLexer(in, errListener);
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        stOTTRParser parser = new stOTTRParser(commonTokenStream);
        // Only use our own ErrorListener
        parser.removeErrorListeners();
        parser.addErrorListener(errListener);
        return parser;
    }

    public ResultStream<Instance> apply(CharStream in) {

        // Make parser
        ErrorToMessageListener errListener = new ErrorToMessageListener();
        stOTTRParser parser = makeParser(in, errListener);
        stOTTRParser.StOTTRDocContext document = parser.stOTTRDoc();

        // Parse prefixes
        SPrefixParser prefixParser = new SPrefixParser();
        Result<Map<String, String>> prefixRes = prefixParser.visit(document);
        this.prefixes = prefixRes.get();
        this.termParser = new STermParser(this.prefixes);

        // Parse instances
        // Below code will not be executed if prefixes are not present
        ResultStream<Instance> instances = prefixRes.mapToStream(_ignore -> {

            Stream<Result<Instance>> results = document
                .statement() // List of statments
                .stream()
                .map(stmt -> visitStatement(stmt));
            
            return new ResultStream<>(results);
        });

        // TODO: Somehow put these messages on returned instances,
        //       see https://gitlab.com/ottr/lutra/lutra/issues/148
        errListener.getMessages().printMessages();
        return instances;
    }

    @Override
    public Result<Instance> visitStatement(stOTTRParser.StatementContext ctx) {

        if (ctx.instance() == null) { // Not an instance
            return Result.empty(); // TODO: Decide on error or ignore?
        }

        return visitInstance(ctx.instance());
    }

    @Override
    public Result<Instance> visitInstance(stOTTRParser.InstanceContext ctx) {

        // Parse template name
        Result<String> iriRes = termParser
            .visitIri(ctx.templateName().iri())
            .map(iri -> ((IRITerm) iri).getIRI());

        // Parse arguments and possible list expander
        SArgumentListParser argumentListParser = new SArgumentListParser(termParser);
        Result<ArgumentList> argsRes = argumentListParser.visitInstance(ctx);

        return Result.zip(iriRes, argsRes, (iri, args) -> new Instance(iri, args));
    }
}
