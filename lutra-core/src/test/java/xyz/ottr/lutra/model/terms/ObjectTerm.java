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

public class ObjectTerm extends AbstractTerm<Object> {

    public static ObjectTerm var(Object identifier) {
        return new ObjectTerm(identifier, true);
    }

    public static ObjectTerm cons(Object identifier) {
        return new ObjectTerm(identifier, false);
    }

    private ObjectTerm(Object identifier, boolean variable) {
        super(identifier);
        setVariable(variable);
        setType(getIntrinsicType());
    }

    private ObjectTerm(Object identifier) {
        this(identifier, false);
    }

    @Override
    public Optional<Term> unify(Term other) {
        if (isVariable()
                || !other.isVariable() && getIdentifier().equals(other.getIdentifier())) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Term shallowClone() {
        return ObjectTerm.cons(getIdentifier());
    }

}
