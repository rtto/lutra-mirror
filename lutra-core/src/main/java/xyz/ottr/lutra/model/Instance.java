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

import java.util.Objects;
import org.apache.jena.shared.PrefixMapping;

public class Instance {

    private final String iri;
    private final ArgumentList args;

    public Instance(String iri, ArgumentList args) {
        this.iri = iri;
        this.args = args;
    }

    public String getIRI() {
        return this.iri;
    }

    public ArgumentList getArguments() {
        return this.args;
    }

    /**
     * Returns a String similar to toString(), but
     * IRIs are written as qnames according to the
     * argument PrefixMapping.
     */
    public String toString(PrefixMapping prefixes) {
        String pre = this.args.hasCrossExpander() ? "x | " : this.args.hasZipExpander() ? "z | " : "";
        String qname = prefixes.qnameFor(this.iri);
        return pre + ((qname == null) ? this.iri : qname) + this.args.toString(prefixes);
    }

    @Override
    public String toString() {
        String pre = this.args.hasCrossExpander() ? "x | " : this.args.hasZipExpander() ? "z | " : "";
        return pre + this.iri + this.args.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.iri, this.args);
    }

    @Override
    public boolean equals(Object o) {
        return this == o 
                || Objects.nonNull(o) 
                        && getClass() == o.getClass()
                        && Objects.equals(this.getIRI(), ((Instance) o).getIRI())
                        && Objects.equals(this.getArguments(), ((Instance) o).getArguments());
    }
}
