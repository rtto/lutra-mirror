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

    /**
     * Used to record the known type of the template:
     * - UNDEFINED: We have only observed the IRI used as a template in
     *              other templates, i.e. we do not (yet) have a definition of the template
     * - BASE: The node is added as a base template 
     * - SIGNATURE: The node is added as a signature template
     * - DEFINITION: The template has been added with a definition
     */
    enum Type { UNDEFINED, BASE, SIGNATURE, DEFINITION }

    private ParameterList parameters;
    private Type type;
    private final String iri;

    public TemplateNode(String iri, Type type) {
        this.iri = iri;
        this.type = type;
    }

    public TemplateNode(String iri, ParameterList parameters, Type type) {
        this(iri, type);
        this.parameters = parameters;
    }

    public String getIRI() {
        return this.iri;
    }

    public Type getType() {
        return this.type;
    }

    public void addParameters(ParameterList parameters) {
        this.parameters = parameters;
    }

    public ParameterList getParameters() {
        return this.parameters;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isBase() {
        return this.type.equals(Type.BASE);
    }

    public boolean isSignature() {
        return this.type.equals(Type.SIGNATURE);
    }

    public boolean isDefinition() {
        return this.type.equals(Type.DEFINITION);
    }

    public boolean isUndefined() {
        return this.type.equals(Type.UNDEFINED);
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
