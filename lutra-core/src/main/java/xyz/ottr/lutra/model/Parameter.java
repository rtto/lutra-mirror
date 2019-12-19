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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.terms.Term;

@Getter
@Builder
public class Parameter implements TermWrapper {

    private final @NonNull Term term;
    private final boolean nonBlank;
    private final boolean optional;
    private final Term defaultValue;

    public boolean hasDefaultValue() {
        return Objects.nonNull(this.defaultValue);
    }

    public static List<Parameter> of(Term... terms) {
        return Arrays.stream(terms)
            .map(t -> builder().term(t).build())
            .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return toString(OTTR.getDefaultPrefixes());
    }

    public String toString(PrefixMapping prefixMapping) {

        StringBuilder str = new StringBuilder();
        if (this.optional) {
            str.append("?");
        }
        if (this.nonBlank) {
            str.append("!");
        }

        str.append(this.term.toString(prefixMapping));

        if (hasDefaultValue()) {
            str.append(" = ");
            str.append(this.defaultValue.toString(prefixMapping));
        }

        return str.toString();
    }

}
