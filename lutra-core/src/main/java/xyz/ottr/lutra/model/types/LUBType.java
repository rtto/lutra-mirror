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
import xyz.ottr.lutra.OTTR;

@EqualsAndHashCode(callSuper = true)
public class LUBType extends ComplexType {


    public LUBType(BasicType inner) {
        super(inner);
    }

    @Override
    public String getOuterIRI() {
        return OTTR.TypeURI.LUB;
    }

    @Override
    public boolean isSubTypeOf(TermType other) {

        return other instanceof LUBType
            // For other LUB-types, LUB<P> is only subtype of itself
            ? this.inner.equals(((LUBType) other).getInner())

            // LUB<P> subtype of P, and thus all subtypes of P
            : this.inner.isSubTypeOf(other);
    }

    @Override
    public boolean isCompatibleWith(TermType other) {
        return isSubTypeOf(other) || other.isSubTypeOf(this.inner);
    }

    @Override
    public TermType removeLUB() {
        return getInner();
    }

    @Override
    public String toString() {
        return "LUB<" + this.inner + ">";
    }

}
