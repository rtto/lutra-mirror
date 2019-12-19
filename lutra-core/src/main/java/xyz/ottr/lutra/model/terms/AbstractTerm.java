package xyz.ottr.lutra.model.terms;

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
import lombok.Setter;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.Substitution;
import xyz.ottr.lutra.model.types.LUBType;
import xyz.ottr.lutra.model.types.TermType;
import xyz.ottr.lutra.model.types.TypeRegistry;

@Getter
@Setter
public abstract class AbstractTerm<T> implements Term {

    private final T identifier;
    protected @NonNull TermType type;
    protected boolean variable;

    AbstractTerm(T identifier) {
        this.identifier = identifier;
    }

    @Override
    public TermType getIntrinsicType() {
        return this.variable ? TypeRegistry.TOP : new LUBType(TypeRegistry.TOP);
    }

    @Override
    public int hashCode() {
        return this.identifier.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this == o
            || Objects.nonNull(o)
            && getClass() == o.getClass()
            && this.variable == ((Term) o).isVariable()
            && Objects.equals(this.identifier, ((AbstractTerm) o).identifier);
    }

    @Override
    public Term apply(Substitution substitution) {
        return Objects.requireNonNullElse(substitution.get(this), this);
    }

    public String toString(PrefixMapping prefixes) {
        return (this.variable ? "?" : "")
            + prefixes.shortForm(identifier.toString())
            + " : " + prefixes.shortForm(type.toString());
    }

    public String toString() {
        return toString(OTTR.getDefaultPrefixes());
    }
}
