package xyz.ottr.lutra.model.types;

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

import org.apache.jena.rdf.model.Resource;

public class BasicType implements TermType {

    private final Resource iri;

    protected BasicType(Resource iri) {
        this.iri = iri;
    }

    public String getIri() {
        return this.iri.toString();
    }

    @Override
    public boolean isSubTypeOf(TermType supertype) {
        if (this.equals(TypeFactory.getBotType())) {
            return true;
        }
        return supertype instanceof BasicType
            && TypeFactory.isSubTypeOf(this, (BasicType) supertype);
    }

    @Override
    public boolean isCompatibleWith(TermType other) {
        return isSubTypeOf(other);
    }

    @Override
    public String toString() {
        return this.iri.toString();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof BasicType
            && getIri().equals(((BasicType) other).getIri());
    }

    @Override
    public int hashCode() {
        return getIri().hashCode();
    }
}
