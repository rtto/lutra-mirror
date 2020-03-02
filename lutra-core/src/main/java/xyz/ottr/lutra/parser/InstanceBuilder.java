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
import java.util.stream.Collectors;

import lombok.Builder;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.ListExpander;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;

public abstract class InstanceBuilder {

    @Builder
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static Result<Instance> createInstance(Result<String> iri, Result<List<Argument>> arguments,
                                                   Result<ListExpander> listExpander) {

        iri = Result.nullToEmpty(iri, Message.error("Missing IRI. An instance must have an IRI (that refers to a signature)."));
        arguments = Result.nullToEmpty(arguments, Message.error("Missing argument list. An instance must have "
            + "a (possibly empty) list of arguments."));
        listExpander = Result.nullToEmpty(listExpander);

        var builder = Result.of(Instance.builder());
        builder.addResult(iri, Instance.InstanceBuilder::iri);
        builder.addResult(arguments, Instance.InstanceBuilder::arguments);
        builder.addResult(listExpander, Instance.InstanceBuilder::listExpander);

        if (Result.allIsPresent(iri, arguments)) {
            var instance = builder.map(Instance.InstanceBuilder::build);
            validateListExpanders(instance);
            return instance;
        } else {
            return Result.empty(builder);
        }
    }

    private static void validateListExpanders(Result<Instance> instance) {
        instance.ifPresent(i -> {
            var forExpansion = i.getArguments().stream()
                .filter(Argument::isListExpander)
                .collect(Collectors.toList());

            if (i.hasListExpander() && forExpansion.isEmpty()) {
                instance.addError("The instance is marked with the list expander "
                    + i.getListExpander() + ", but no arguments are marked for list expansion.");
            } else if (!i.hasListExpander() && !forExpansion.isEmpty()) {
                instance.addError("The instance has arguments which are marked for list expansion:"
                    + forExpansion + ", but the instance is not marked with a list expander");
            }
        });
    }

}
