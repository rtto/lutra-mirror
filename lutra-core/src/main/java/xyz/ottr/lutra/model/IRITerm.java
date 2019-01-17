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

import xyz.ottr.lutra.model.types.TypeFactory;

public class IRITerm extends ResourceTerm {

    private final String iri;

    public IRITerm(String iri) {
        this.iri = iri;
        super.type = TypeFactory.getConstantType(this);
    }

    public String getIRI() {
        return this.iri;
    }

    @Override 
    public IRITerm shallowClone() {
        IRITerm t = new IRITerm(this.iri);
        t.setIsVariable(super.isVariable());
        return t;
    }

    @Override
    public Optional<Term> unify(Term other) {

        if ((other instanceof TermList 
             || other instanceof IRITerm
             || other instanceof BlankNodeTerm
             || other instanceof NoneTerm)
            && (isVariable() || !other.isVariable() && equals(other))) { 
            return Optional.of(other);
        }

        return Optional.empty();
    } 

    @Override
    public String getIdentifier() {
        return getIRI();
    }
}
