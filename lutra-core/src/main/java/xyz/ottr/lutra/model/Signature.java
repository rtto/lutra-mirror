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

import java.util.Objects;

import lombok.Getter;
import lombok.NonNull;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.OTTR;

@Getter
public class Signature {

    private final @NonNull String iri;
    private final @NonNull ParameterList parameters;
    private final boolean isBaseTemplate;

    protected Signature(String iri, ParameterList parameters, boolean isBaseTemplate) {
        this.iri = iri;
        this.parameters = parameters;
        this.isBaseTemplate = isBaseTemplate;
        setVariables();
    }

    protected Signature(String iri, ParameterList parameters) {
        this(iri, parameters, false);
    }

    final void setVariables() {
        this.parameters.asList().forEach(p -> p.setVariable(true));
    }

    public String toString(PrefixMapping prefixes) {
        return prefixes.shortForm(this.iri) + this.parameters.toString(prefixes);
    }

    @Override
    public String toString() {
        return toString(OTTR.getDefaultPrefixes());
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
                        && Objects.equals(this.getIri(), ((Template) o).getIri());
    }
}
