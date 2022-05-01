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
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
     * @param consoleStream
     *      A PrintStream used to write output to console
     * @return
     *      Returns message handler containing error or warn messages if file creation failed,
     */
    public MessageHandler init(String filePath, PrintStream consoleStream) {

        msgs = new MessageHandler();
        
        if (consoleStream != null) {
            this.consoleStream = consoleStream;
        }
        
        fileOutput = (filePath != null) ? true : false;
        
    
        if (fileOutput) {
            try {
                Path parentDirs = Paths.get(filePath).getParent(); 
                if (parentDirs != null) {
                    Files.createDirectories(parentDirs); //create parent directories
                }
                buffWriter = new BufferedWriter(new FileWriter(filePath, Charset.forName("UTF-8")));
            } catch (Exception ex) {
                msgs.add(Message.error("Error opening file: " + filePath + ".", ex));
            }
        }
        return msgs;
    }

    /**
     * Write string contents to file or console
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
            } catch (Exception ex) {
                msgs.add(Message.error("Error writing file.", ex));
            }
        }
        return msgs;
    }

    /**
     * Flush contents of BufferedWriter and PrintStream
     *  
     * @return
     *      Returns message handler.
     */
    public MessageHandler flush() {
        
        if (consoleStream != null) {
            this.consoleStream.flush();
        }
        
        if (fileOutput) {
            try {
                this.buffWriter.flush();
            } catch (Exception ex) {
                msgs.add(Message.error("Error flushing contents.", ex));
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
            } catch (Exception ex) {
                msgs.add(Message.error("Error closing writer.", ex));
            }
        }
        return msgs;
    }
}
