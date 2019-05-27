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
import java.io.Writer;
import java.util.Map;

public class SPrefixWriter {

    public static void write(Map<String, String> prefixes, Writer writer) {

        try {
            for (Map.Entry<String, String> nsln : prefixes.entrySet()) {
                writer.write("@prefix " + nsln.getKey() + ": <" + nsln.getValue() + "> .\n");
            }
            writer.write("\n");
        } catch (IOException ex) {
            System.err.println(ex.toString()); // TODO
        }
    }
}
