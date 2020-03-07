package xyz.ottr.lutra.bottr;

/*-
 * #%L
 * lutra-bottr
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

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import xyz.ottr.lutra.wottr.WOTTR;

public class BOTTR extends WOTTR {

    // Classes
    public static final Resource InstanceMap = getResource(ns + "InstanceMap");

    public static final Resource JDBCSource = getResource(ns + "JDBCSource");
    public static final Resource SPARQLEndpointSource = getResource(ns + "SPARQLEndpointSource");
    public static final Resource RDFFileSource = getResource(ns + "RDFFileSource");
    public static final Resource H2Source = getResource(ns + "H2Source");

    public static final List<Resource> sources = List.of(JDBCSource, SPARQLEndpointSource, RDFFileSource, H2Source);
    
    // Properties
    public static final Property template = getProperty(ns + "template");
    public static final Property argumentMaps = getProperty(ns + "argumentMaps");
    public static final Property source = getProperty(ns + "source");
    public static final Property query = getProperty(ns + "query");

    public static final Property sourceURL = getProperty(ns + "sourceURL");

    public static final Property username = getProperty(ns + "username");
    public static final Property password = getProperty(ns + "password");
    public static final Property jdbcDriver = getProperty(ns + "jdbcDriver");

    public static final Property nullValue = getProperty(ns + "nullValue");
    public static final Property languageTag = getProperty(ns + "languageTag");
    public static final Property labelledBlankPrefix = getProperty(ns + "labelledBlankPrefix");
    public static final Property listSep = getProperty(ns + "listSep");
    public static final Property listStart = getProperty(ns + "listStart");
    public static final Property listEnd = getProperty(ns + "listEnd");

    public static final Property translationSettings = getProperty(ns + "translationSettings");
    public static final Property translationTable = getProperty(ns + "translationTable");
    public static final Property inValue = getProperty(ns + "inValue");
    public static final Property outValue = getProperty(ns + "outValue");
    public static final Property entry = getProperty(ns + "entry");

    // TOKENS
    public static final String THIS_DIR = "@@THIS_DIR@@";

    public enum Settings {
        ;
        /**
         * Global setting for adding a LIMIT to SPARQL SELECT queries.
         */
        @Getter @Setter private static int RDFSourceQueryLimit = -1;
    }
}
