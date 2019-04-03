package xyz.ottr.lutra.bottr.model;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import java.util.function.Supplier;

import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.Instance;
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

public class Map implements Supplier<ResultStream<Instance>> {

    protected final Source source;
    protected final String query;
    protected final String templateIRI;
    protected final ValueMap valueMap;

    public Map(Source source, String query, String templateIRI, ValueMap valueMap) {
        this.source = source;
        this.query = query;
        this.templateIRI = templateIRI;
        this.valueMap = valueMap;
    }
    
    @Override
    public ResultStream<Instance> get() {
        return source.execute(this.query).innerFlatMap(row -> mapToInstance(row));
    }

    @SuppressWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "work in progress")
    public ResultStream<Instance> mapToInstance(Source.Row row) {
        new Instance(this.templateIRI, new ArgumentList(this.valueMap.getTerms(row)));
        
        return null; // TODO
    }

    public String getQuery() {
        return this.query;
    }
        
    public String getTemplateIRI() {
        return this.templateIRI;
    }
    
}
