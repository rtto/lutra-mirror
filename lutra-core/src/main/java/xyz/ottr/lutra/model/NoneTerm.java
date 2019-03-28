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

import java.util.Optional;

//import xyz.ottr.lutra.ROTTR;
import xyz.ottr.lutra.model.types.TypeFactory;

public class NoneTerm extends Term {

    public NoneTerm() {
        super();
        super.isVariable = false;
        super.type = TypeFactory.getBotType();
    }

    public Object getIdentifier() {
        return "none";
    }

    public void setIsVariable(boolean isVariable) {
        if (isVariable) {
            throw new UnsupportedOperationException("Cannot set a NoneTerm to a varible.");
        }
    }

    public Optional<Term> unify(Term other) {
        if (equals(other)) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    public boolean isBlank() {
        return false;
    }

    public Term shallowClone() {
        return new NoneTerm();
    }

    // TODO: Decide if Nones should be equal or not
    //@Override
    //public boolean equals(Object other) {
    //    return this == other; // A NoneTerm is never equal to other NoneTerms
    //}

    //@Override
    //public int hashCode() {
    //    return System.identityHashCode(this); // See equals()
    //}
}
