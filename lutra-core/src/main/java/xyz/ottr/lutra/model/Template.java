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
import java.util.stream.Collectors;

import lombok.Getter;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.model.terms.TermList;
import xyz.ottr.lutra.model.types.TermType;

@SuppressWarnings("PMD.UselessOverridingMethod")
@Getter
public class Template extends Signature {

    private final Set<Instance> pattern; // TODO? Set as NonNull?

    public static Template createTemplate(String iri, ParameterList parameters, Set<Instance> pattern) {
        return new Template(iri, parameters, pattern);
    }

    public static Template createTemplate(Signature signature, Set<Instance> pattern) {
        return new Template(signature.getIri(), signature.getParameters(), pattern);
    }

    public static Signature createBaseTemplate(String iri, ParameterList parameters) {
        return new Signature(iri, parameters, true);
    }

    public static Signature createSignature(String iri, ParameterList parameters) {
        return new Signature(iri, parameters, false);
    }

    public static Signature createSignature(String iri) {
        return new Signature(iri, null);
    }
    
    private Template(String iri, ParameterList parameters, Set<Instance> pattern) {
        super(iri, parameters);
        this.pattern = pattern;
        setVariableFlagsAndTypes();
    }

    private void setVariableFlagsAndTypes() {
        if (getParameters() == null) {
            return;
        }

        Map<Object, TermType> idTypes = new HashMap<>();
        for (Term var : getParameters().asList()) {
            var.setVariable(true);
            idTypes.put(var.getIdentifier(), var.getType());
        }

        if (this.pattern != null) {
            this.pattern
                .forEach(instance ->
                    setVariableFlagsAndTypes(instance.getArguments().asList(), idTypes));
        }
    }

    private void setVariableFlagsAndTypes(List<Term> terms, Map<Object, TermType> idTypes) {
        terms
            .forEach(term -> {
                if (term instanceof TermList) {
                    TermList tl = (TermList) term;
                    setVariableFlagsAndTypes(tl.asList(), idTypes);
                    tl.recomputeType();
                } else if (idTypes.containsKey(term.getIdentifier())) {
                    term.setVariable(true);
                    term.setType(idTypes.get(term.getIdentifier()));
                }
            });
    }

    @Override
    public String toString(PrefixMapping prefixes) {
        String signature = super.toString(prefixes);

        String pattern = getPattern().stream()
            .map(ins -> "\t" + ins.toString(prefixes))
            .collect(Collectors.joining(",\n", "{", "}"));

        return signature + " ::\n" + pattern + " .";
    }

    @Override
    public String toString() {
        return toString(OTTR.getDefaultPrefixes());
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
