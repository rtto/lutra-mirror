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
import xyz.ottr.lutra.model.ListExpander;
import xyz.ottr.lutra.model.types.ComplexType;
import xyz.ottr.lutra.model.types.LUBType;
import xyz.ottr.lutra.model.types.ListType;
import xyz.ottr.lutra.model.types.NEListType;

/**
 * Constants used for *writing* (and not for parsing) stOTTR syntax.
 * Parsing stOTTR is defined by an Antrl grammar.
 */

// TODO: move to writer package?
// TODO: none, LUB, List, NEList and expanders should come from core.
public class STOTTR {

    public enum Terms {
        ;
        public static final String none = "none";
        public static final String listStart = "(";
        public static final String listEnd = ")";
        public static final String listSep = ", ";
        public static final String variablePrefix = "?";
        public static final String blankPrefix = "_:";
        public static final String insArgStart = "(";
        public static final String insArgEnd = ")";
        public static final String insArgSep = ", ";
        public static final String annoArgSep = ",";
    }

    public enum Types {
        ;
        public static final String lub = "LUB";
        public static final String list = "List";
        public static final String neList = "NEList";
        public static final String innerTypeStart = "<";
        public static final String innerTypeEnd = ">";

        public static final BidiMap<Class<? extends ComplexType>, String> map;

        static {
            BidiMap<Class<? extends ComplexType>, String> tempMap = new DualHashBidiMap<>();
            tempMap.put(LUBType.class, lub);
            tempMap.put(ListType.class, list);
            tempMap.put(NEListType.class, neList);
            map = UnmodifiableBidiMap.unmodifiableBidiMap(tempMap);
        }
    }

    public enum Statements {
        ;
        public static final String commentStart = "# ";
        public static final String bodyStart = "{";
        public static final String bodyEnd = "}";
        public static final String bodyInsSep = ",";
        public static final String baseBody = "BASE";
        public static final String signatureSep = " :: ";
        public static final String annotationStart = "@@";
        public static final String annotationSep = ",";
        public static final String statementEnd = " .";
    }

    public enum Expanders {
        ;
        public static final String cross = "cross";
        public static final String zipMin = "zipMin";
        public static final String zipMax = "zipMax";
        public static final String expanderSep = " | ";
        public static final String expander = "++";

        public static final BidiMap<ListExpander, String> map;

        static {
            BidiMap<ListExpander, String> tempMap = new DualHashBidiMap<>();
            tempMap.put(ListExpander.cross, cross);
            tempMap.put(ListExpander.zipMin, zipMin);
            tempMap.put(ListExpander.zipMax, zipMax);
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
