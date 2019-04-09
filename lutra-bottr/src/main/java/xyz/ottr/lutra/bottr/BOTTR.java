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

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import xyz.ottr.lutra.wottr.WOTTR;

public class BOTTR extends WOTTR {

    // Classes
    public static final Resource InstanceMap = getResource("InstanceMap");
    public static final Resource SQLSource = getResource("SQLSource");
    
    // Properties
    public static final Property template = getProperty("template");
    public static final Property valueMap = getProperty("valueMap");
    public static final Property source = getProperty("source");
    public static final Property query = getProperty("query");
    
    public static final Property username = getProperty("username");
    public static final Property password = getProperty("password");
    public static final Property jdbcDriver = getProperty("jdbcDriver");
    public static final Property jdbcDatabaseURL = getProperty("jdbcDatabaseURL");
    
}
