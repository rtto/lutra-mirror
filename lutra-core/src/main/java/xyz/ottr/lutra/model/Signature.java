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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.system.Result;

@Getter
@Builder(builderMethodName = "superbuilder")
public class Signature implements ModelElement {

    private final @NonNull String iri;
    private final @NonNull @Singular List<Parameter> parameters;

    @Override
    public String toString() {
        return toString(OTTR.getDefaultPrefixes());
    }

    @Override
    public String toString(PrefixMapping prefixes) {
        return prefixes.shortForm(this.iri)
            + this.parameters.stream()
                .map(t -> t.toString(prefixes))
                .collect(Collectors.joining(", ", "[ ", " ]"));
    }

    @Override
    public int hashCode() {
        return this.iri.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this == o 
                || Objects.nonNull(o) 
                        && getClass() == o.getClass()
                        && Objects.equals(this.iri, ((Signature) o).iri);
    }

    @Override
    public Result<? extends Signature> validate() {

        var result = Result.of(this);

        // duplicate variable names
        var duplicateVarNames = this.getParameters().stream()
                .collect(Collectors.groupingBy(p -> p.getTerm().getIdentifier())) // group parameters by variable name
                .entrySet()
                .stream()
                .filter(e -> e.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        if (!duplicateVarNames.isEmpty()) {
            result.addError("Parameter variables must be unique. Signature contains multiple occurrences "
                + "of the same variable name: " + duplicateVarNames);
        }

        return result;
    }
}
