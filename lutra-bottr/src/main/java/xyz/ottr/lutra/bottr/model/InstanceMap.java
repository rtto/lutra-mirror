package xyz.ottr.lutra.bottr.model;

import java.util.function.Supplier;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
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

@Getter
@Builder
public class InstanceMap<V> implements Supplier<ResultStream<Instance>> {

    private final @NonNull Source<V> source;
    private final @NonNull String query;
    private final @NonNull String templateIRI;
    private final @NonNull ArgumentMaps<V> argumentMaps;

    @Override
    public ResultStream<Instance> get() {
        return this.source.execute(this.query, this.argumentMaps)
            .innerMap(args -> new Instance(this.templateIRI, args));
    }

}
