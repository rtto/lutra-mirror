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

import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import xyz.ottr.lutra.model.types.Type;
import xyz.ottr.lutra.parser.TypeParser;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.wottr.util.RDFNodes;

public class WTypeParser implements Function<RDFNode, Result<Type>> {

    public Result<Type> apply(RDFNode node) {

        return node.canAs(RDFList.class)
            ? apply(node.as(RDFList.class))
            : toURI(node).flatMap(TypeParser::type);
    }

    public Result<Type> apply(RDFList node) {

        var uris = node.as(RDFList.class).asJavaList().stream()
                .map(this::toURI)
                .collect(Collectors.toList());

        return Result.aggregate(uris)
            .flatMap(TypeParser::type);
    }

    private Result<String> toURI(RDFNode node) {
        return RDFNodes.castURIResource(node)
            .map(Resource::getURI);
    }
}
