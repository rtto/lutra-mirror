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

/**
 * Represents the type a term can have. Term types are crucial when type checking 
 * template arguments against template parameters, and for unifying templates.
 * A basic term type can be identified by its IRI.
 * There are also the type-constructors NEList, List and LUB which can be nested.
 * A term type may be subtype or compatible with other term types; 
 * Basic term types are declared in a external vocabulary and may therefore not be constructed, 
 * but may be gotten from the TypeRegistry.
 * @author martige leifhka
 */
public interface Type {

    boolean isSubTypeOf(Type other);

    default boolean isProperSubTypeOf(Type other) {
        return isSubTypeOf(other) && !this.equals(other);
    }

    boolean isCompatibleWith(Type other);

    default boolean isIncompatibleWith(Type other) {
        return !isCompatibleWith(other);
    }

    Type removeLUB();
    
    String toString();
}

