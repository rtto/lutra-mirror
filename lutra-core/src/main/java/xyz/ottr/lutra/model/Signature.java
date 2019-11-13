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
import xyz.ottr.lutra.model.terms.Term;

public class Signature {

    private final String iri;
    private final ParameterList params;
    private final boolean isBaseTemplate;

    public Signature(String iri, ParameterList params, boolean isBaseTemplate) {
        this.iri = iri;
        this.params = params;
        this.isBaseTemplate = isBaseTemplate;
        setVariables();
    }

    public Signature(String iri, ParameterList params) {
        this(iri, params, false);
    }

    public Signature(String iri) {
        this(iri, null);
    }

    private void setVariables() {
        if (this.params != null) {
            for (Term var : this.params.asList()) {
                if (var != null) {
                    var.setIsVariable(true);
                }
            }
        }
    }

    public String getIRI() {
        return this.iri;
    }

    public ParameterList getParameters() {
        return this.params;
    }

    public boolean isBaseTemplate() {
        return this.isBaseTemplate;
    }

    /**
     * Returns a String similar to toString(), but
     * IRIs are written as qnames according to the
     * argument PrefixMapping.
     */
    public String toString(PrefixMapping prefixes) {
        String qheadStr = prefixes.qnameFor(this.iri);
        String headStr = (qheadStr == null) ? this.iri : qheadStr;
        headStr += (this.params == null) ? "(...)" : this.params.toString(prefixes);
        return headStr;
    }

    @Override
    public String toString() {
        String headStr = this.iri;
        headStr += (this.params == null) ? "(...)" : this.params.toString();
        return headStr;
    }

    @Override
    public int hashCode() {
        return this.iri.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this == o 
                || Objects.nonNull(o) 
                        && getClass() == o.getClass()
                        && Objects.equals(this.getIRI(), ((Template) o).getIRI());
    }
}
