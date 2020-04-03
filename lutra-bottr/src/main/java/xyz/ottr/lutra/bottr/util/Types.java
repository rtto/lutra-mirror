package xyz.ottr.lutra.bottr.util;

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

import java.util.Optional;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import xyz.ottr.lutra.model.types.Type;
import xyz.ottr.lutra.model.types.TypeRegistry;

public enum Types {

    ;

    public static Type getIntrinsicType(RDFNode value) {
        return value.isLiteral()
            ? getIntrinsicType(value.asLiteral())
            : getIntrinsicType(value.asResource());
    }

    public static Type getIntrinsicType(Resource value) {
        return value.isURIResource()
            ? TypeRegistry.IRI
            : TypeRegistry.TOP;
    }

    public static Type getIntrinsicType(Literal value) {
        return Optional.ofNullable(value.getDatatypeURI())
            .map(TypeRegistry::asType)
            .orElse(TypeRegistry.LITERAL);
    }
}
