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

    private final @NonNull Set<Instance> pattern;

    private Template(String iri, List<Parameter> parameters, Set<Instance> instances) {
        super(iri, parameters);
        this.pattern = instances;
        updatePatternVariables();
    }

    /**
     *
     * @param iri
     * @param parameters
     * @param instances
     * @param isEmptyPattern The isEmptyPattern flag is a safety precaution for avoiding inadvertently creating templates
     *                       with empty patterns.
     * @return
     */
    @Builder
    public static Template create(String iri, @Singular List<Parameter> parameters, @Singular Set<Instance> instances,
                                  boolean isEmptyPattern) {

        if (isEmptyPattern != instances.isEmpty()) {
            var message = "Creating template with "
                + (instances.isEmpty() ? "empty" : "non-empty")
                + " pattern, but isEmptyPattern flag is " + isEmptyPattern
                + ".";
            throw new IllegalArgumentException(message);
        }

        return new Template(iri, parameters, instances);
    }

    /**
     * Propagates type and variable setting of parameter terms to identical terms in instances, i.e.,
     * turns instance terms into variables.
     */
    private void updatePatternVariables() {
        // Collect parameter types
        Map<Object, TermType> parameterTypes = getParameters().stream()
            .map(Parameter::getTerm)
            .collect(Collectors.toMap(Term::getIdentifier, Term::getType, (t1, t2) -> t1));

        this.pattern.forEach(instance ->
            setTermsToVariables(
                instance.getArguments().stream()
                    .map(Argument::getTerm)
                    .collect(Collectors.toList()), parameterTypes));
    }

    private void setTermsToVariables(List<Term> terms, Map<Object, TermType> parameterTypes) {
        terms.forEach(term -> {
            if (term instanceof ListTerm) {
                ListTerm tl = (ListTerm) term;
                setTermsToVariables(tl.asList(), parameterTypes);
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
            + this.pattern.stream()
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
