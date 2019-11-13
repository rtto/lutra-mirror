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

public class BlankNodeTerm extends ResourceTerm {

    private static int newID = 0;
    private final String label;

    public BlankNodeTerm(String label) {
        this.label = label;
        super.type = TypeFactory.getConstantType(this);
    }

    public BlankNodeTerm() {
        this("_blank" + getNewID());
    }

    private static int getNewID() {
        newID += 1;
        return newID;
    }

    public String getLabel() {
        return this.label;
    }

    @Override
    public BlankNodeTerm shallowClone() {
        BlankNodeTerm t = new BlankNodeTerm(this.label);
        t.setIsVariable(super.isVariable());
        return t;
    }

    @Override
    public Optional<Term> unify(Term other) {
        return Optional.of(other); // TODO: Correct if other is a variable?
    } 

    @Override
    public String getIdentifier() {
        return getLabel();
    }
}
