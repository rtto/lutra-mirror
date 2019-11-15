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
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.types.TermType;

@Getter
@Setter
@AllArgsConstructor
public abstract class Term {

    @NonNull private TermType type;
    private boolean isVariable;

    public abstract Object getIdentifier();

    public abstract Optional<Term> unify(Term other);

    public static Optional<Term> unify(Term t1, Term t2) {
        return t1.unify(t2).or(() -> t2.unify(t1));
    }

    // TODO: only needed in Clustering, perhaps find better way of
    //       handling RDF-specifics in lutra-core
    public abstract boolean isBlank();

    /**
     * Returns the TermType that the variable Term
     * has as default if no type is given, and is only based on the Term itself,
     * and therefore not usage.
     */
    public TermType getVariableType() {
        // The default type of a variable is the same as
        // for a constant term, except that we remove
        // any surrounding LUB. E.g. an IRI variable
        // has default type IRI.
        return getType().removeLUB();
    }

    /**
     * Returns a shallow clone of this Term.
     */
    public abstract Term shallowClone();

    @Override
    public boolean equals(Object o) {
        return this == o 
                || Objects.nonNull(o) 
                        && getClass() == o.getClass()
                        && this.isVariable() == ((Term) o).isVariable()
                        && Objects.equals(this.getIdentifier(), ((Term) o).getIdentifier());
    }

    @Override
    public int hashCode() {
        //return Objects.hash(getIdentifier(), isVariable);
        return getIdentifier().hashCode(); // Variable may change, so not part of hash TODO: Fix?
    }

    public String toString(PrefixMapping prefixes) {
        return prefixes.shortForm(getIdentifier().toString());
    }

    @Override
    public String toString() {
        return toString(OTTR.getDefaultPrefixes());
    }
}
