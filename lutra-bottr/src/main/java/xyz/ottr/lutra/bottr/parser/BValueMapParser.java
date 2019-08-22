package xyz.ottr.lutra.bottr.parser;

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

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.Resource;

import xyz.ottr.lutra.bottr.BOTTR;
import xyz.ottr.lutra.bottr.model.ValueMap;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.wottr.util.ModelSelector;
import xyz.ottr.lutra.wottr.util.RDFNodes;

class BValueMapParser implements Function<RDFList, Result<ValueMap>> {

    private final Model model;

    BValueMapParser(Model model) {
        this.model = model;
    }

    @Override
    public Result<ValueMap> apply(RDFList list) {

        List<Result<String>> listElements = ResultStream.innerOf(list.asJavaList())
            .mapFlatMap(node -> RDFNodes.cast(node, Resource.class))
            .mapFlatMap(r -> ModelSelector.getRequiredURIResourceObject(this.model, r, BOTTR.type))
            .innerMap(Resource::getURI)
            .collect(Collectors.toList());

        return Result.aggregate(listElements)
            .map(types -> new ValueMap(this.model, types));
    }
}
