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
import org.apache.jena.vocabulary.RDF;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.types.BasicType;
import xyz.ottr.lutra.model.types.LUBType;
import xyz.ottr.lutra.model.types.ListType;
import xyz.ottr.lutra.model.types.NEListType;
import xyz.ottr.lutra.model.types.TermType;

public class TypeFactory {

    public static Resource createRDFType(Model model, TermType type) {

        if (type instanceof BasicType) {
            return model.createResource(((BasicType) type).getIRI());
        } else {
            return createComplexRDFType(model, type);
        }
    }

    // TODO split into one method for ComplexType and add use (must add) ComplextType.getIRI,
    // and one for BasicType.
    private static RDFList createComplexRDFType(Model model, TermType type) {
        if (type instanceof ListType) {
            RDFList rest = createComplexRDFType(model, ((ListType) type).getInner());
            return rest.cons(RDF.List);
        } else if (type instanceof NEListType) {
            RDFList rest = createComplexRDFType(model, ((NEListType) type).getInner());
            return rest.cons(model.createResource(OTTR.TypeURI.NEList));
        } else if (type instanceof LUBType) {
            RDFList rest = createComplexRDFType(model, ((LUBType) type).getInner());
            return rest.cons(model.createResource(OTTR.TypeURI.LUB));
        } else {
            RDFList nil = model.createList();
            Resource rdfType = model.createResource(((BasicType) type).getIRI());
            return nil.cons(rdfType);
        }
    }
}
