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
 * A basic term type can be identified by its IRI *and its localname while ignoring casing*. 
 * There are also the type-constructors NEList, List and LUB which can be nested.
 * A term type may be subtype or compatible with other term types; 
 * Basic term types are declared in a external vocabulary and may therefore not be constructed, 
 * but may be gotten from the TypeFactory. 
 * @author martige leifhka
 */
public interface TermType {

    boolean isSubTypeOf(TermType other);

    boolean isCompatibleWith(TermType other);

    default boolean isIncompatibleWith(TermType other) {
        return !isCompatibleWith(other);
    }
    
    String toString();
}

