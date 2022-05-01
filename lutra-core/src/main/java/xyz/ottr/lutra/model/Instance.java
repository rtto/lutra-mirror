package xyz.ottr.lutra.model;

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
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.system.Result;

@Getter
@EqualsAndHashCode
@Builder(toBuilder = true)
public class Instance implements ModelElement, HasApplySubstitution<Instance>  {

    private final String iri;
    private final @Singular List<Argument> arguments;
    private final ListExpander listExpander;

    public boolean hasListExpander() {
        return this.listExpander != null;
    }

    /* TODO: remove?
    public boolean hasListExpander(int index) {
        return this.arguments.get(index).isListExpander();
    }
    */

    @Override
    public String toString() {
        return toString(OTTR.getDefaultPrefixes());
    }

    @Override
    public String toString(PrefixMapping prefixes) {
        return (Objects.isNull(this.listExpander) ? "" : this.listExpander + " | ")
            + prefixes.shortForm(this.iri)
            + this.arguments.stream()
                .map(t -> t.toString(prefixes))
                .collect(Collectors.joining(", ", "(", ")"));
    }

    @Override
    public Instance apply(Substitution substitution) {
        return this.toBuilder()
            .clearArguments()
            .arguments(substitution.apply(this.arguments))
            .build();
    }

    @Override
    public Result<Instance> validate() {

        var result = Result.of(this);

        // has list expander iff has arguments marked for list expansion.
        var forExpansion = this.getArguments().stream()
            .filter(Argument::isListExpander)
            .collect(Collectors.toList());
        if (this.hasListExpander() && forExpansion.isEmpty()) {
            result.addError("The instance is marked with the list expander "
                + this.getListExpander() + ", but no arguments are marked for list expansion.");
        } else if (!this.hasListExpander() && !forExpansion.isEmpty()) {
            result.addError("The instance has arguments which are marked for list expansion:"
                + forExpansion + ", but the instance is not marked with a list expander");
        }

        return result;
    }
}
