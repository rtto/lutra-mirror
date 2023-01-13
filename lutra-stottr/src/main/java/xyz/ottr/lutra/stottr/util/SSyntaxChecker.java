package xyz.ottr.lutra.stottr.util;

/*-
 * #%L
 * xyz.ottr.lutra:lutra-stottr
 * %%
 * Copyright (C) 2018 - 2020 University of Oslo
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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import lombok.Setter;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import xyz.ottr.lutra.Space;
import xyz.ottr.lutra.stottr.antlr.stOTTRLexer;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;
import xyz.ottr.lutra.stottr.parser.ErrorToMessageListener;
import xyz.ottr.lutra.stottr.parser.SParserUtils;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.MessageHandler;

@Setter
public class SSyntaxChecker {

    private final MessageHandler messageHandler;
    private boolean printParseTree = false;
    private boolean printTokens = false;

    public SSyntaxChecker(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public MessageHandler checkString(String content) {
        return check(CharStreams.fromString(content));
    }

    public MessageHandler checkFile(Path file) throws IOException {
        return check(CharStreams.fromFileName(file.toString()));
    }

    private MessageHandler check(CharStream charStream) {
        var errorListener = new ErrorToMessageListener(this.messageHandler);

        var parser = SParserUtils.makeParser(charStream, errorListener);

        this.messageHandler.add(Message.info("Parsing input..."));
        parser.stOTTRDoc();
        this.messageHandler.add(Message.info("Done parsing input."));


        var tokens = new CommonTokenStream(parser.getTokenStream().getTokenSource());
        tokens.fill();

        if (this.printTokens) {
            this.messageHandler.add(Message.info(printTokens(tokens)));
        }

        if (this.printParseTree) {
            this.messageHandler.add(Message.info(printParseTree(parser)));
        }

        return this.messageHandler;
    }

    private String printTokens(CommonTokenStream tokens) {

        StringBuilder str = new StringBuilder();

        for (Token t : tokens.getTokens()) {

            String symbolicName = stOTTRLexer.VOCABULARY.getSymbolicName(t.getType());
            String literalName = stOTTRLexer.VOCABULARY.getLiteralName(t.getType());

            str.append(String.format(Locale.ENGLISH,
                "  %-20s '%s'" + Space.LINEBR,
                symbolicName == null ? literalName : symbolicName,
                t.getText()
                    .replace("\r", "\r")
                    .replace("\n", "\n")
                    .replace("\t", "\t")));
        }
        return str.toString();
    }

    private String printParseTree(stOTTRParser parser) {
        ParserRuleContext context = parser.stOTTRDoc();
        String tree = context.toStringTree(parser);
        return printPrettyLispTree(tree);
    }

    private String printPrettyLispTree(String tree) {

        var str = new StringBuilder();
        int indentation = 1;
        for (char c : tree.toCharArray()) {
            if (c == '(') {
                if (indentation > 1) {
                    str.append(Space.LINEBR);
                }
                str.append(Space.INDENT.repeat(Math.max(0, indentation)));
                indentation++;
            } else if (c == ')') {
                indentation--;
            }
            str.append(c);
        }

        return str.toString();
    }

}


