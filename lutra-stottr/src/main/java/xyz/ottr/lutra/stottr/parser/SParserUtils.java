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
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.stottr.antlr.stOTTRLexer;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;

public class SParserUtils {
    
    public static stOTTRLexer makeLexer(CharStream in, ErrorToMessageListener errListener) {

        stOTTRLexer lexer = new stOTTRLexer(in);
        // Only use our own ErrorListener
        lexer.removeErrorListeners();
        lexer.addErrorListener(errListener);
        return lexer;
    }

    public static stOTTRParser makeParser(CharStream in, ErrorToMessageListener errListener) {

        stOTTRLexer lexer = makeLexer(in, errListener);
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        stOTTRParser parser = new stOTTRParser(commonTokenStream);
        // Only use our own ErrorListener
        parser.removeErrorListeners();
        parser.addErrorListener(errListener);
        return parser;
    }

    public static <T> Result<T> parseString(String toParse, SBaseParserVisitor<T> visitor) {

        ErrorToMessageListener errListener = new ErrorToMessageListener();
        stOTTRParser parser = makeParser(CharStreams.fromString(toParse), errListener);
        stOTTRParser.StOTTRDocContext document = parser.stOTTRDoc();

        Result<T> res = visitor.visit(document);

        errListener.getMessages().printMessages();
        return res;
    }
    
    ///
    /// Utility methods used for making error messages
    ///

    public static int getLineOf(ParserRuleContext ctx) {
        return ctx.getStart().getLine();
    }

    public static int getColumnOf(ParserRuleContext ctx) {
        return ctx.getStart().getCharPositionInLine();
    }

    public static String getLineAndColumnString(ParserRuleContext ctx) {
        return " at line " + getLineOf(ctx)
            + " column " + getColumnOf(ctx);
    }
}
