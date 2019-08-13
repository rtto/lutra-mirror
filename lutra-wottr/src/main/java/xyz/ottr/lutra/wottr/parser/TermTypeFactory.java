package xyz.ottr.lutra.wottr.parser;

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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.types.BasicType;
import xyz.ottr.lutra.model.types.LUBType;
import xyz.ottr.lutra.model.types.ListType;
import xyz.ottr.lutra.model.types.NEListType;
import xyz.ottr.lutra.model.types.TermType;
import xyz.ottr.lutra.model.types.TypeFactory;
import xyz.ottr.lutra.result.Result;

public class TermTypeFactory implements Function<RDFNode, Result<TermType>> {

    public Result<TermType> apply(RDFNode node) {

        if (node.canAs(RDFList.class)) {
            return parseComplexType(node.as(RDFList.class).asJavaList().iterator());
        } else {
            List<RDFNode> typeList = new LinkedList<>();
            typeList.add(node);
            return parseComplexType(typeList.iterator());
        }
    }

    private Result<TermType> parseSimpleType(Resource node) {

        TermType type = TypeFactory.getType(node.getURI()); 

        if (type == null) {
            return Result.error("Expected a resource denoting a simple type, but no simple type with IRI "
                + node.getURI() + " exists.");
        } else {
            return Result.of(type);
        }
    }

    private Result<TermType> parseComplexType(Iterator<RDFNode> complexType) {

        if (!complexType.hasNext()) {
            return Result.error("Expected a resource denoting a basic or complex type, but got nothing.");
        }
        RDFNode typeNode = complexType.next();
        if (!typeNode.isResource() || typeNode.asResource().getURI() == null) {
            return Result.error("A type constructor must be denoted by an IRI, but got " + typeNode.toString());
        }
        Resource type = typeNode.asResource();

        Result<TermType> rest = parseComplexType(complexType);
        if (type.getURI().equals(OTTR.TypeURI.NEList)) {
            return rest.flatMap(inner -> Result.of(new NEListType(inner)));
        } else if (type.equals(RDF.List)) {
            return rest.flatMap(inner -> Result.of(new ListType(inner)));
        } else if (type.getURI().equals(OTTR.TypeURI.LUB)) {
            if (rest.isPresent() && !(rest.get() instanceof BasicType)) {
                return Result.error("Expected simple type as argument to LUB-type, but got "
                    + rest.get().toString());
            }
            return rest.flatMap(inner -> Result.of(new LUBType((BasicType) inner)));
        } else if (!rest.isPresent()) {
            return parseSimpleType(type);
        } else {
            return Result.error("Unrecognized type constructor " + type.toString() + ".");
        }
    }
}
