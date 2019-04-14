package xyz.ottr.lutra.bottr.model;

import java.util.function.Supplier;

import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;

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

public class InstanceMap implements Supplier<ResultStream<Instance>> {

    private final Source<?> source;
    private final String query;
    private final String templateIRI;
    private final ValueMap valueMap;

    public InstanceMap(Source<?> source, String query, String templateIRI, ValueMap valueMap) {
        this.source = source;
        this.query = query;
        this.templateIRI = templateIRI;
        this.valueMap = valueMap;        
    }
    
    @Override
    public ResultStream<Instance> get() {
        return source.execute(this.query).mapFlatMap(row -> mapToInstance(row));
    }

    private Result<Instance> mapToInstance(Record<?> row) {
        return valueMap.apply(row).flatMap(argList -> Result.of(new Instance(this.templateIRI, argList)));
    }
    
    public String getQuery() {
        return this.query;
    }
        
    public String getTemplateIRI() {
        return this.templateIRI;
    }

}
