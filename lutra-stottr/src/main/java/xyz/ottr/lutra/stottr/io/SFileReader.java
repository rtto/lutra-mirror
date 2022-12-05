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

import java.io.File;
import java.io.IOException;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import xyz.ottr.lutra.io.InputReader;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;

public class SFileReader implements InputReader<String, CharStream> {

    public ResultStream<CharStream> apply(String filename) {

        if (new File(filename).length() == 0) {
            return ResultStream.of(Result.warning("Empty file: " + filename));
        }

        try {
            return ResultStream.innerOf(CharStreams.fromFileName(filename));
        } catch (IOException ex) {
            return ResultStream.of(Result.error("Reading stOTTR file: '" + filename + "'.", ex));
        }
    }
}
