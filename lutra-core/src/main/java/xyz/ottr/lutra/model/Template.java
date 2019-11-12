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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.jena.shared.PrefixMapping;

import xyz.ottr.lutra.model.types.TermType;

@SuppressWarnings("PMD.UselessOverridingMethod")
public class Template extends TemplateSignature {

    //private Set<Instance> head;
    private final Set<Instance> body;
    
    public Template(String iri, ParameterList params, Set<Instance> body) {
        super(iri, params);
        this.body = body;
        setVariableFlagsAndTypes();
    }

    public Template(TemplateSignature signature, Set<Instance> body) {
        this(signature.getIRI(), signature.getParameters(), body);
    }

    public Set<Instance> getBody() {
        return this.body;
    }

    private void setVariableFlagsAndTypes() {
        if (getParameters() == null) {
            return;
        }

        Map<Object, TermType> idTypes = new HashMap<>();
        for (Term var : getParameters().asList()) {
            var.setIsVariable(true);
            idTypes.put(var.getIdentifier(), var.getType());
        }

        if (this.body != null) {
            this.body.stream()
                .forEach(instance ->
                    setVariableFlagsAndTypes(instance.getArguments().asList(), idTypes));
        }
    }

    private void setVariableFlagsAndTypes(List<Term> terms, Map<Object, TermType> idTypes) {
        terms.stream()
            .forEach(term -> {
                if (term instanceof TermList) {
                    TermList tl = (TermList) term;
                    setVariableFlagsAndTypes(tl.asList(), idTypes);
                    tl.recomputeType();
                } else if (idTypes.containsKey(term.getIdentifier())) {
                    term.setIsVariable(true);
                    term.setType(idTypes.get(term.getIdentifier()));
                }
            });
    }
        

    @Override
    public String toString(PrefixMapping prefixes) {
        String headStr = super.toString(prefixes);
        headStr += " ::\n";
        StringBuilder bodyStr = new StringBuilder();
        for (Instance ins : getBody()) {
            bodyStr.append("    " + ins.toString(prefixes));
            bodyStr.append("\n");
        }
        return headStr + bodyStr.toString();
    }

    @Override
    public String toString() {
        String headStr = super.toString();
        headStr += " ::\n";
        StringBuilder bodyStr = new StringBuilder();
        for (Instance ins : getBody()) {
            bodyStr.append("    " + ins.toString());
            bodyStr.append("\n");
        }
        return headStr + bodyStr.toString();
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
