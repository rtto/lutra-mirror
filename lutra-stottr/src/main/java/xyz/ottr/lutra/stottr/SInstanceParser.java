package xyz.ottr.lutra.stottr;

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
import org.antlr.v4.runtime.CommonTokenStream;

import xyz.ottr.lutra.stottr.antlr.stOTTRBaseListener;
import xyz.ottr.lutra.stottr.antlr.stOTTRLexer;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;

public class SParser {

    public SParser() {
    }

    public void parseString(CharStream in) {
        stOTTRLexer lexer = new stOTTRLexer(in);
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        stOTTRParser parser = new stOTTRParser(commonTokenStream);
 
        stOTTRParser.StatementContext stmtContext = parser.statement();                
        stOTTRBaseListener listener = new stOTTRBaseListener();                
        listener.enterStatement(stmtContext);
    }
}
