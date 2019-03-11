package xyz.ottr.lutra;

/*-
 * #%L
 * lutra-core
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

public class ROTTR  {

    public static final String rdfTemplates = "http://tpl.ottr.xyz/rdf/0.1/"; 
    public static final String triple = rdfTemplates + "Triple"; 

    private static final String ns = "http://spec.ottr.xyz/rottr/0/types#";
    public static final String namespace = ns;
    
    //public static final String none = ns + "none";
    public static final String subTypeOf = ns + "subTypeOf";
    public static final String termType = ns + "Type";
    public static final String NEListType = ns + "NEList";
    public static final String LUBType = ns + "LUB";
}
