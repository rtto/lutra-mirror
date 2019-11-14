package xyz.ottr.lutra.tabottr;

/*-
 * #%L
 * lutra-tab
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

public enum TabOTTR {
    ;

    public static final String TOKEN = "#OTTR";
    
    public static final String INSTRUCTION_TEMPLATE = "template";
    public static final String INSTRUCTION_PREFIX = "prefix";
    public static final String INSTRUCTION_END = "end";
    
    public static final String TYPE_IRI = "iri";
    public static final String TYPE_BLANK = "blank";
    public static final String TYPE_TEXT = "text";
    public static final String TYPE_AUTO = "auto";
    public static final String TYPE_LIST_POSTFIX = "+";
    
    public static final String VALUE_LIST_SEPARATOR = "|";
    public static final String VALUE_FRESH_BLANK = "*";
    public static final String VALUE_BLANK_NODE_PREFIX = "_:";
    public static final String VALUE_LANGUAGE_TAG_PREFIX = "@@";
    public static final String VALUE_DATATYPE_TAG_PREFIX = "^^";
}
