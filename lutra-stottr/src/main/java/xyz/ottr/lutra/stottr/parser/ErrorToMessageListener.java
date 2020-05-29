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

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.system.Result;

public class ErrorToMessageListener extends BaseErrorListener {

    private final MessageHandler messageHandler;

    public ErrorToMessageListener(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public ErrorToMessageListener() {
        this(new MessageHandler());
    }
    
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
        int charPositionInLine, String msg, RecognitionException e) {
        
        String err = "Syntax error at line " + line  + " col " + charPositionInLine + ": " + msg;
        this.messageHandler.add(Result.empty(Message.error(err)));
    }

    public MessageHandler getMessageHandler() {
        return this.messageHandler;
    }
}
