package xyz.ottr.lutra.wottr.util;

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

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.writer.RDFNodeWriter;

public enum RDFNodes {

    ; // singleton enum

    public static <X extends RDFNode> Result<X> cast(RDFNode node, Class<X> type) {
        return node.canAs(type)
            ? Result.of(node.as(type))
            : Result.error("Expected instance of " + type.getSimpleName()
                + ", but found " + node.getClass().getSimpleName() + ": " + RDFNodeWriter.toString(node));
    }

    public static Result<Resource> castURIResource(RDFNode node) {
        Result<Resource> resource = cast(node, Resource.class);
        if (resource.isPresent() && !resource.get().isURIResource()) {
            return Result.error("Expected instance of URIResource, but got " +  RDFNodeWriter.toString(resource.get()) + ".");
        } else {
            return resource;
        }
    }

}
