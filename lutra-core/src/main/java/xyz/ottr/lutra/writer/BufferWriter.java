package xyz.ottr.lutra.writer;

/*-
 * #%L
 * xyz.ottr.lutra:lutra-core
 * %%
 * Copyright (C) 2018 - 2021 University of Oslo
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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.MessageHandler;


public abstract class BufferWriter {
    private BufferedWriter buffWriter;
    private PrintStream consoleStream;
    private boolean fileOutput;
    private MessageHandler msgs;

    /**
     * Initialize Buffered Writer for writing to file, if output is to be written to console, set appropriate flags.
     *  
     * @param filePath
     *      A String containing file path to write to.
     * @param consoleOutput
     *      A boolean specifying if results should be written to console
     * @return
     *      Returns message handler containing error or warn messages if file creation failed,
     */
    public MessageHandler init(String filePath, PrintStream consoleStream) {

        msgs = new MessageHandler();
        
        if (consoleStream != null) {
            this.consoleStream = consoleStream;
            msgs.add(Message.info("Output will be written to console"));
        }
        
        if (filePath != null) {
            this.fileOutput = true;
        } else {
            this.fileOutput = false;
            msgs.add(Message.warning("No file path given"));
        }
    
        if (fileOutput) {
            try {
                buffWriter = new BufferedWriter(new FileWriter(filePath, Charset.forName("UTF-8")));
            } catch (IOException ex) {
                Message err = Message.error("Error opening file: " + ex.getMessage());
                msgs.add(err);
            }
        }
        return msgs;
    }

    /**
     * Write string contents to file
     *  
     * @param contents
     *      file contents to write
     * @return
     *      Returns message handler
     */
    public MessageHandler write(String contents) {
        if (consoleStream != null) {
            this.consoleStream.println(contents);
        }

        if (fileOutput) {
            try {
                buffWriter.write(contents);
            } catch (IOException ex) {
                Message err = Message.error("Error writing file: " + ex.getMessage());
                msgs.add(err);
            }
        }
        return msgs;
    }

    /**
     * Flush contents of BufferedWriter
     *  
     * @return
     *      Returns message handler.
     */
    public MessageHandler flush() {
        if (fileOutput) {
            try {
                this.buffWriter.flush();
            } catch (IOException ex) {
                Message err = Message.error("Error flushing contents: " + ex.getMessage());
                msgs.add(err);
            }
        }
        return msgs;
    }

    /**
     * Close writer
     *  
     * @return
     *      Returns message handler.
     */
    public MessageHandler close() {
        if (fileOutput) {
            try {
                this.buffWriter.close();
            } catch (IOException ex) {
                Message err = Message.error("Error closing writer " + ex.getMessage());
                msgs.add(err);
            }
        }
        return msgs;
    }
}
