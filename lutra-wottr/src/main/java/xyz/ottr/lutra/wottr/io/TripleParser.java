package xyz.ottr.lutra.wottr.io;

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
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Model;

import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.wottr.WTemplateFactory;

public class TripleParser implements Function<Model, ResultStream<Instance>> {

    public ResultStream<Instance> apply(Model model) {

        Stream<Result<Instance>> parsedTriples = model.listStatements()
            .toSet()
            .stream()
            .filter(s -> !WTemplateFactory.isRedundant(s))
            .map(WTemplateFactory::createTripleInstance);
        return new ResultStream<Instance>(parsedTriples);
    }
}
