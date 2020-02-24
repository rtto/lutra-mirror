package xyz.ottr.lutra.parser;

/*-
 * #%L
 * lutra-core
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

import lombok.Builder;
import lombok.NonNull;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.ListExpander;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;

public abstract class InstanceParser<E> implements Function<E, ResultStream<Instance>> {

    @Builder
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static Result<Instance> createInstance(@NonNull Result<String> iri,
        @NonNull Result<List<Argument>> arguments, Result<ListExpander> listExpander) {

        var builder = Result.of(Instance.builder());
        builder.addResult(iri, Instance.InstanceBuilder::iri);
        builder.addResult(arguments, Instance.InstanceBuilder::arguments);
        builder.addResult(listExpander, Instance.InstanceBuilder::listExpander);
        var instance = builder.map(Instance.InstanceBuilder::build);

        validateListExpanders(instance);

        return instance;
    }

    private static void validateListExpanders(Result<Instance> instance) {
        instance.ifPresent(i -> {
            var forExpansion = i.getArguments().stream()
                .filter(arg -> arg.isListExpander())
                .collect(Collectors.toList());

            if (i.hasListExpander() && forExpansion.isEmpty()) {
                instance.addError("Instance is marked with listExpander "
                    + i.getListExpander() + ", but no arguments are marked for list expansion.");
            } else if (!i.hasListExpander() && !forExpansion.isEmpty()) {
                instance.addError("Instance has arguments which are marked for list expansion:"
                    + forExpansion + ", but the instance is not marked with listExpander");
            }
        });
    }

}
