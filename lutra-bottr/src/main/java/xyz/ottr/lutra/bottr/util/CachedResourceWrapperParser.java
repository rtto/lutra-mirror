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

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import xyz.ottr.lutra.system.Result;

public abstract class CachedResourceWrapperParser<X> extends ResourceWrapperParser<X> {

    private static final Map<Map.Entry<Model, Resource>, Result> cache = new HashMap<>();

    protected CachedResourceWrapperParser(Resource resource) {
        super(resource);
    }

    @Override
    public Result<X> get() {
        Map.Entry<Model, Resource> key = new AbstractMap.SimpleEntry<>(this.model, this.resource);
        return cache.computeIfAbsent(key, pair -> getResult(pair.getValue()));
    }
}
