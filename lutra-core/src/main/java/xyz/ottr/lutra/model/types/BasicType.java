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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import xyz.ottr.lutra.OTTR;

@Getter
@EqualsAndHashCode
public class BasicType implements Type {

    private final String iri;

    protected BasicType(String iri) {
        this.iri = iri;
    }

    @Override
    public boolean isSubTypeOf(Type supertype) {
        if (this.equals(TypeRegistry.BOT)) {
            return true;
        }
        return supertype instanceof BasicType
            && TypeRegistry.isSubTypeOf(this, (BasicType) supertype);
    }

    @Override
    public boolean isCompatibleWith(Type other) {
        return isSubTypeOf(other);
    }

    @Override
    public Type removeLUB() {
        return this;
    }

    @Override
    public String toString() {
        return OTTR.getDefaultPrefixes().shortForm(this.iri);
    }

}
