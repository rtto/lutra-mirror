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

public class OTTR  {

    private static final String ns = "http://ns.ottr.xyz/0.4/";
    
    public static final String prefix = "ottr";
    public static final String namespace = ns;
    
    public static class Bases {
        public static final String Triple = ns + "Triple";
    }

    public static class Types {
        public static final String Type = ns + "Type";
        public static final String subTypeOf = ns + "subTypeOf";
        
        public static final String NEList = ns + "NEList";
        public static final String LUB = ns + "LUB";
    }
    
    public static class Files {
        public static final String StdTypes = "types.owl.ttl";
    }
}
