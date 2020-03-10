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

import java.util.Optional;

import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.model.HasApplySubstitution;
import xyz.ottr.lutra.model.types.TermType;

public interface Term extends HasApplySubstitution<Term> {

    Object getIdentifier();

    void setVariable(boolean variable);

    boolean isVariable();

    void setType(TermType term);

    TermType getType();

    /**
     * Returns the TermType that the variable Term has as default if no type is given, and is only based on the
     * Term itself, and therefore not usage.
     */
    default TermType getVariableType() {
        // The default type of a variable is the same as for a constant term, except that we remove
        // any surrounding LUB. E.g. an IRI variable has default type IRI.
        return getType().removeLUB();
    }

    Optional<Term> unify(Term other);

    static Optional<Term> unify(Term t1, Term t2) {
        return t1.unify(t2).or(() -> t2.unify(t1));
    }

    Term shallowClone();

    String toString(PrefixMapping prefixMapping);

}
