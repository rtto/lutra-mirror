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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.model.terms.TermList;

@Getter
public class ParameterList extends AbstractTermList {

    // TODO: Bug, must be based on indices
    private final Set<Term> nonBlanks;
    private final Set<Term> optionals;
    private final Map<Term, Term> defaultValues;

    public ParameterList(TermList parameters, Set<Term> nonBlanks, Set<Term> optionals, Map<Term, Term> defaultValues) {
        super(parameters);
        this.nonBlanks = Objects.requireNonNullElse(nonBlanks, new HashSet<>());
        this.optionals = Objects.requireNonNullElse(optionals, new HashSet<>());
        this.defaultValues = Objects.requireNonNullElse(defaultValues, new HashMap<>());
    }
    
    public ParameterList(List<Term> parameters, Set<Term> nonBlanks, Set<Term> optionals, Map<Term, Term> defaultValues) {
        this(new TermList(parameters), nonBlanks, optionals, defaultValues);
    }
    
    public ParameterList(List<Term> parameters) {
        this(parameters, null, null, null);
    }
    
    public ParameterList(Term... elems) {
        this(List.of(elems), null, null, null);
    }

    public boolean isNonBlank(Term param) {
        return this.nonBlanks.contains(param);
    }

    public boolean isNonBlank(int index) {
        return isNonBlank(this.termList.get(index));
    }

    public boolean hasDefaultValue(Term param) {
        return this.defaultValues.containsKey(param);
    }

    public boolean hasDefaultValue(int index) {
        return hasDefaultValue(get(index));
    }

    public Term getDefaultValue(Term param) {
        return this.defaultValues.get(param);
    }

    public Term getDefaultValue(int index) {
        return getDefaultValue(get(index));
    }

    public boolean isOptional(int index) {
        return isOptional(get(index));
    }

    public boolean isOptional(Term e) {
        return this.optionals.contains(e);
    }

    public ParameterList shallowCloneTerms() {
        List<Term> clonedTerms = new LinkedList<>();
        Set<Term> clonedNonBlanks = new HashSet<>();
        Set<Term> clonedOptionals = new HashSet<>();
        Map<Term, Term> clonedDefaults = new HashMap<>();

        for (Term t : this.termList.asList()) {
            Term nt = t.shallowClone();
            clonedTerms.add(nt);
            if (isNonBlank(t)) {
                clonedNonBlanks.add(nt);
            }
            if (isOptional(t)) {
                clonedOptionals.add(nt);
            }
            if (hasDefaultValue(t)) {
                clonedDefaults.put(nt, getDefaultValue(t).shallowClone());
            }
        }
        return new ParameterList(clonedTerms, clonedNonBlanks, clonedOptionals, clonedDefaults);
    }

    /**
     * Returns a String similar to toString(), but
     * IRIs are written as qnames according to the
     * argument PrefixMapping.
     */
    public String toString(PrefixMapping prefixes) {
        return asList().stream()
            .map(t -> (this.optionals.contains(t) ? "?" : "")
                + t.toString(prefixes))
            .collect(Collectors.joining(", ", "(", ")"));
    }

    @Override
    public String toString() {
        return toString(OTTR.getDefaultPrefixes());
    }

    @Override
    public boolean equals(Object o) {
        return this == o
            || this.getClass() == o.getClass()
            && this.termList.asList().equals(((ParameterList) o).termList.asList())
            && this.defaultValues.equals(((ParameterList) o).defaultValues)
            && this.optionals.equals(((ParameterList) o).optionals);
    }

    @Override
    public int hashCode() {
        return this.termList.asList().hashCode() + 3 * this.defaultValues.hashCode() + 5 * this.optionals.hashCode();
    }
}
