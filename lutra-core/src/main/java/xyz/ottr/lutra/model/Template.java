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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

import org.apache.jena.shared.PrefixMapping;

import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.terms.ListTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.model.types.TermType;

@SuppressWarnings("PMD.UselessOverridingMethod")
@Getter
public class Template extends Signature {

    @Singular private final @NonNull Set<Instance> instances;

    @Builder(builderMethodName = "superbuilder") // Cannot use @SuperBuilder as we need to run our own constructor.
    public Template(String iri, @Singular List<Parameter> parameters, @Singular Set<Instance> instances) {
        super(iri, parameters);
        this.instances = instances;
        setVariableFlagsAndTypes();
    }

    private void setVariableFlagsAndTypes() {
        // Collect parameter types
        Map<Object, TermType> parameterTypes = getParameters().stream()
            .map(Parameter::getTerm)
            .collect(Collectors.toMap(Term::getIdentifier, Term::getType, (t1, t2) -> t1));

        this.instances.forEach(instance ->
            setVariableFlagsAndTypes(
                instance.getArguments().stream()
                    .map(Argument::getTerm)
                    .collect(Collectors.toList()), parameterTypes));
    }

    private void setVariableFlagsAndTypes(List<Term> terms, Map<Object, TermType> parameterTypes) {
        terms.forEach(term -> {
            if (term instanceof ListTerm) {
                ListTerm tl = (ListTerm) term;
                setVariableFlagsAndTypes(tl.asList(), parameterTypes);
                tl.recomputeType();
            } else if (parameterTypes.containsKey(term.getIdentifier())) {
                term.setVariable(true);
                term.setType(parameterTypes.get(term.getIdentifier()));
            }
        });
    }

    @Override
    public String toString() {
        return toString(OTTR.getDefaultPrefixes());
    }

    public String toString(PrefixMapping prefixes) {
        return super.toString(prefixes)
            + " ::\n"
            + this.instances.stream()
                .map(ins -> "\t" + ins.toString(prefixes))
                .collect(Collectors.joining(",\n", "{", "}"))
            + " .";
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
