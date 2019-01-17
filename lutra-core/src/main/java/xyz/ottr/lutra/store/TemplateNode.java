package xyz.ottr.lutra.store;

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

import xyz.ottr.lutra.model.ParameterList;
//import xyz.ottr.lutra.model.Substitution;
import xyz.ottr.lutra.model.Term;

public class TemplateNode {

    private ParameterList parameters;
    private boolean isBaseTemplate;
    private String iri;

    public TemplateNode(String iri) {
        this.iri = iri;
    }

    public TemplateNode(String iri, ParameterList parameters, boolean isBaseTemplate) {
        this(iri);
        this.parameters = parameters;
        this.isBaseTemplate = isBaseTemplate;
    }

    public String getIRI() {
        return this.iri;
    }

    public void addParameters(ParameterList parameters) {
        this.parameters = parameters;
    }

    public ParameterList getParameters() {
        return this.parameters;
    }

    public void setIsBaseTemplate(boolean isBaseTemplate) {
        this.isBaseTemplate = isBaseTemplate;
    }

    public boolean isBaseTemplate() {
        return this.isBaseTemplate;
    }
    
    public boolean isOptional(int index) {
        return getParameters().isOptional(getParameters().get(index));
    }

    public boolean isOptional(Term e) {
        return getParameters().isOptional(e);
    }

    @Override
    public String toString() {
        return getIRI() + Objects.toString(parameters, "(...)");
    }
}
