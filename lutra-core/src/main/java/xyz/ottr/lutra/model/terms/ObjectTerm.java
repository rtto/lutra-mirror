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

import xyz.ottr.lutra.model.types.TypeFactory;

public class ObjectTerm extends Term {

    private final Object identifier;

    public ObjectTerm(Object identifier, boolean isVariable) {
        super.isVariable = isVariable;
        this.identifier = identifier;
        if (isVariable) {
            super.type = TypeFactory.getVariableType(this);
        } else {
            super.type = TypeFactory.getConstantType(this);
        }
    }

    public ObjectTerm(Object identifier) {
        this(identifier, false);
    }

    @Override
    public Object getIdentifier() {
        return this.identifier;
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
        return new ObjectTerm(getIdentifier());
    }

    @Override
    public boolean isBlank() {
        return false;
    }
}
