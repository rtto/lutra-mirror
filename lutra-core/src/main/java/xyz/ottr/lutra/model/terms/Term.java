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

import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.model.types.TermType;

public abstract class Term {

    protected boolean isVariable;
    protected TermType type;

    public abstract Object getIdentifier();

    public TermType getType() {
        return this.type;
    }

    public void setType(TermType type) {
        this.type = type;
    }

    public boolean isVariable() {
        return this.isVariable;
    }

    public void setIsVariable(boolean isVariable) {
        this.isVariable = isVariable;
    }

    public abstract Optional<Term> unify(Term other);

    public static Optional<Term> unify(Term t1, Term t2) {

        Optional<Term> u1 = t1.unify(t2);
        return u1.isPresent() ? u1 : t2.unify(t1);
    }

    // TODO: only needed in Clustering, perhaps find better way of
    //       handling RDF-specifics in lurta-core 
    public abstract boolean isBlank();

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

    /**
     * Returns a String similar to toString(), but
     * IRIs are written as qnames according to the
     * argument PrefixMapping.
     */
    public String toString(PrefixMapping prefixes) {
        String qname = prefixes.qnameFor(getIdentifier().toString());
        return (qname == null) ? toString() : qname;
    }

    @Override
    public String toString() {
        return getIdentifier().toString();
    }
}
