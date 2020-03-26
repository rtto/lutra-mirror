package xyz.ottr.lutra;

/*-
 * #%L
 * lutra-core
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

import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;

public enum RDFTurtle {
    ;

    public static final String plainLiteralDatatype = XSD.xstring.getURI();
    public static final String langStringDatatype = RDF.dtLangString.getURI(); // TODO add this type to the type hierarchy

    public static final String prefix = "@prefix ";
    public static final String prefixSep = ": ";
    public static final String prefixEnd = " .";

    public static final String qnameSep = ":";

    public static final String literalLang = "@";
    public static final String literalType = "^^";
    
    public static String fullURI(String uri) {
        return "<" + uri + ">";
    }

    public static String literal(String value) {
        return "\"" + value + "\"";
    }

}
