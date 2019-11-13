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

import java.util.Locale;
import java.util.Objects;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.OTTR;

@Getter
@EqualsAndHashCode
public class Instance {

    private final String iri;
    private final ArgumentList arguments;

    public Instance(String iri, ArgumentList arguments) {
        this.iri = iri;
        this.arguments = arguments;
    }

    public String toString(PrefixMapping prefixes) {

        String expander = Objects.toString(this.arguments.getListExpander(), "").toLowerCase(Locale.ENGLISH);
        if (!expander.isEmpty()) {
            expander += " | ";
        }
        return expander
            + prefixes.shortForm(this.iri)
            + this.arguments.toString(prefixes);
    }

    @Override
    public String toString() {
        return toString(OTTR.getDefaultPrefixes());
    }
}
