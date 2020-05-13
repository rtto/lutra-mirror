package xyz.ottr.lutra.bottr.model;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.parser.ArgumentBuilder;
import xyz.ottr.lutra.system.Result;

public class ArgumentMaps<V> implements Function<List<V>, Result<List<Argument>>> {

    private final PrefixMapping prefixMapping;
    private final List<ArgumentMap<V>> argumentMaps;
    private final Source<V> source;

    public ArgumentMaps(PrefixMapping prefixMapping, Source<V> source, List<ArgumentMap<V>> argumentMaps) {
        this.prefixMapping = prefixMapping;
        this.argumentMaps = Collections.unmodifiableList(argumentMaps);
        this.source = source;
    }

    public ArgumentMaps(PrefixMapping prefixMapping, Source<V> source) {
        this(prefixMapping, source, Collections.emptyList());
    }

    @Override
    public Result<List<Argument>> apply(List<V> inValues) {

        List<Result<Term>> outValues;

        if (this.argumentMaps.isEmpty()) {
            outValues = inValues.stream()
                .map(this.source.createArgumentMap(this.prefixMapping))
                .collect(Collectors.toList());
        } else if (inValues.size() != this.argumentMaps.size()) {
            return Result.error("Expected record with " + this.argumentMaps.size() + " elements to match the size of the "
                + this.getClass().getSimpleName() + ", but got a record of size " + inValues.size());
        } else {
            outValues = new ArrayList<>(inValues.size());
            for (int i = 0; i < inValues.size(); i += 1) {
                outValues.add(i, this.argumentMaps.get(i).apply(inValues.get(i)));
            }
        }

        List<Result<Argument>> arguments = outValues.stream()
            .map(t -> ArgumentBuilder.builder().term(t).build())
            .collect(Collectors.toList());

        return Result.aggregate(arguments);
    }

}
