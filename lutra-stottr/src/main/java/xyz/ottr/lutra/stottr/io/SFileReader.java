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

import java.io.IOException;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import xyz.ottr.lutra.io.Files;
import xyz.ottr.lutra.io.InputReader;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;

public class SFileReader implements InputReader<String, CharStream> {

    public ResultStream<CharStream> apply(String filename) {

        if (Files.isRegularFile(filename)) {
            Message error = Files.checkFileEmpty(filename);
            if (error != null) {
                return ResultStream.of(Result.empty(error));
            }
        }

        Result<CharStream> res;
        try {
            res = Result.of(CharStreams.fromFileName(filename));
        } catch (IOException ex) {
            res = Result.error("Reading stOTTR file: '" + filename + "'.", ex);    
        }
        return ResultStream.of(res.setLocation("File " + filename));
    }
}
