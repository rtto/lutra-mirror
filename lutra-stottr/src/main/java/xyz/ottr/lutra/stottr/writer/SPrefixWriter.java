package xyz.ottr.lutra.stottr.writer;

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

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.RDFTurtle;
import xyz.ottr.lutra.Space;

public enum SPrefixWriter {

    ; // util enum

    public static String write(PrefixMapping prefixes) {
        return prefixes.getNsPrefixMap().entrySet().stream()
            .sorted(Comparator.comparing(Map.Entry::getValue))
            .map(entry -> writePrefix(entry.getKey(), entry.getValue()))
            .collect(Collectors.joining(Space.LINEBR));
    }

    private static String writePrefix(String prefix, String namespace) {
        return RDFTurtle.prefixInit + prefix + RDFTurtle.prefixSep + RDFTurtle.fullURI(namespace) + RDFTurtle.prefixEnd;
    }

}
