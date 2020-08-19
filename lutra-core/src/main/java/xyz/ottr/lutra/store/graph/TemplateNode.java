package xyz.ottr.lutra.store.graph;

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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import xyz.ottr.lutra.model.BaseTemplate;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.terms.Term;

@Getter
@Setter
public class TemplateNode {

    /**
     * Used to record the known type of the template:
     * - UNDEFINED: We have only observed the IRI used as a template in
     *              other templates, i.e. we do not (yet) have a definition of the template
     * - BASE: The node is added as a base template 
     * - SIGNATURE: The node is added as a signature template
     * - TEMPLATE: The node has been added with a definition
     */
    enum Type { UNDEFINED, BASE, SIGNATURE, TEMPLATE }

    private static Map<Class, Type> typeMap = Map.of(
        Template.class, Type.TEMPLATE,
        BaseTemplate.class, Type.BASE,
        Signature.class, Type.SIGNATURE
    );

    private final String iri;
    private List<Parameter> parameters;
    private Set<Instance> annotations;
    private Type type;

    TemplateNode(String iri, Type type) {
        this.iri = iri;
        this.type = type;
        this.parameters = null;
    }

    public static TemplateNode.Type getTemplateNodeType(Signature signature) {
        return typeMap.getOrDefault(signature.getClass(), Type.UNDEFINED);
    }

    boolean isBase() {
        return this.type == Type.BASE;
    }

    boolean isSignature() {
        return this.type == Type.SIGNATURE;
    }

    boolean isDefinition() {
        return this.type == Type.TEMPLATE;
    }

    boolean isUndefined() {
        return this.type == Type.UNDEFINED;
    }

    boolean isOptional(int index) {
        return Objects.nonNull(this.parameters)
            && this.parameters.get(index).isOptional();
    }

    boolean isOptional(Term parameterTerm) {
        return Objects.nonNull(this.parameters)
            && this.parameters.stream()
                .filter(p -> p.getTerm().equals(parameterTerm))
                .findFirst()
                .get()
                .isOptional();
    }

    @Override
    public String toString() {
        return this.iri + Objects.toString(this.parameters, "(...)");
    }
}
