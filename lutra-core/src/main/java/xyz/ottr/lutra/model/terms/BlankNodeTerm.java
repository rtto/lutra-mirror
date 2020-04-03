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

import xyz.ottr.lutra.model.Substitution;
import xyz.ottr.lutra.model.types.TypeRegistry;

public class BlankNodeTerm extends AbstractTerm<String> {

    private static long newID = 0L;

    public BlankNodeTerm(String label) {
        super(label, TypeRegistry.LUB_TOP);
    }

    public BlankNodeTerm() {
        this("_blank" + generateNewID());
    }

    private static long generateNewID() {
        newID += 1;
        return newID;
    }

    public String getLabel() {
        return getIdentifier();
    }

    @Override
    public BlankNodeTerm shallowClone() {
        BlankNodeTerm term = new BlankNodeTerm(this.getIdentifier());
        term.setVariable(isVariable());
        return term;
    }

    @Override
    public Term apply(Substitution substitution) {
        return substitution.getOrCompute(this, _ignore -> new BlankNodeTerm());
    }

    @Override
    public Optional<Term> unify(Term other) {
        return Optional.of(other); // TODO: Correct if other is a variable?
    }

}
