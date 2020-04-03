package xyz.ottr.lutra.parser;

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

import java.util.Iterator;
import java.util.List;

import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.types.BasicType;
import xyz.ottr.lutra.model.types.LUBType;
import xyz.ottr.lutra.model.types.ListType;
import xyz.ottr.lutra.model.types.NEListType;
import xyz.ottr.lutra.model.types.Type;
import xyz.ottr.lutra.model.types.TypeRegistry;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.writer.RDFNodeWriter;

public class TypeParser {

    public static Result<Type> type(String... uris) {
        return type(List.of(uris));
    }

    public static Result<Type> type(List<String> uris) {
        return type(uris.iterator());
    }

    private static Result<Type> type(Iterator<String> uris) {

        if (!uris.hasNext()) {
            return Result.error("Error parsing type. Expected uri(s) denoting a type, but got nothing.");
        }

        String first = uris.next();
        Result<Type> rest = type(uris);

        switch (first) {

            case OTTR.TypeURI.NEList:
                return rest.flatMap(inner -> Result.of(new NEListType(inner)));

            case OTTR.TypeURI.List:
                return rest.flatMap(inner -> Result.of(new ListType(inner)));

            case OTTR.TypeURI.LUB:
                if (rest.isPresent() && !(rest.get() instanceof BasicType)) {
                    return Result.error("Error parsing LUB type. "
                        + "Expected a simple type as argument to LUB-type, but got " + rest.get());
                }
                return rest.flatMap(inner -> Result.of(new LUBType((BasicType) inner)));

            default: // must be a simple type
                return !rest.isPresent()
                    ? TypeRegistry.get(first)
                    : Result.error("Unrecognized type: " + RDFNodeWriter.toString(uris) + ".");
        }
    }



}
