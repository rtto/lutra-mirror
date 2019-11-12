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

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.collections4.bidimap.UnmodifiableBidiMap;
import xyz.ottr.lutra.model.ArgumentList;

public class STOTTR {

    // Terms
    public enum Terms {
        ;
        public static final String none = "none";
        public static final String listStart = "(";
        public static final String listEnd = ")";
        public static final String listSep = ",";
        public static final String variablePrefix = "?";
        public static final String insArgStart = "(";
        public static final String insArgEnd = ")";
        public static final String insArgSep = ",";
    }

    public enum Types {
        ;
        public static final String lub = "LUB";
        public static final String list = "List";
        public static final String neList = "NEList";
        public static final String innerTypeStart = "<";
        public static final String innerTypeEnd = ">";
    }

    // Statements
    public enum Statements {
        ;
        public static final String indent = "\t";
        public static final String bodyStart = "{";
        public static final String bodyEnd = "}";
        public static final String bodyInsSep = ",";
        public static final String baseBody = "BASE";
        public static final String signatureSep = "::";
        public static final String statementEnd = ".";
    }

    public enum Expanders {
        ;
        public static final String cross = "cross";
        public static final String zipMin = "zipMin";
        public static final String zipMax = "zipMax";
        public static final String expanderSep = "|";
        public static final String expander = "++";

        public static final BidiMap<String, ArgumentList.Expander> map;

        static {
            BidiMap<String, ArgumentList.Expander> tempMap = new DualHashBidiMap<>();
            tempMap.put(cross, ArgumentList.Expander.CROSS);
            tempMap.put(zipMin, ArgumentList.Expander.ZIPMIN);
            tempMap.put(zipMax, ArgumentList.Expander.ZIPMAX);
            map = UnmodifiableBidiMap.unmodifiableBidiMap(tempMap);
        }
    }

    public enum Parameters {
        ;
        public static final String sigParamsStart = "[";
        public static final String sigParamsEnd = "]";
        public static final String paramSep = ",";
        public static final String optional = "?";
        public static final String nonBlank = "!";
        public static final String defaultValSep = "=";
    }

}
