package xyz.ottr.lutra.wottr.writer;

/*-
 * #%L
 * lutra-wottr
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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.Resource;
import xyz.ottr.lutra.model.types.BasicType;
import xyz.ottr.lutra.model.types.ComplexType;
import xyz.ottr.lutra.model.types.Type;

enum WTypeWriter {

    ; // singleton enum

    public static Resource type(Model model, Type type) {
        return type instanceof BasicType
            ? model.createResource(((BasicType) type).getIri())
            : complexType(model, type);
    }

    private static RDFList complexType(Model model, Type type) {
        if (type instanceof ComplexType) {
            RDFList rest = complexType(model, ((ComplexType) type).getInner());
            return rest.cons(model.createResource(((ComplexType) type).getOuterIRI()));
        } else {
            RDFList nil = model.createList();
            Resource rdfType = model.createResource(((BasicType) type).getIri());
            return nil.cons(rdfType);
        }
    }
}
