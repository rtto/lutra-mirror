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
import java.util.Set;

import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.model.terms.TermList;

public class ParameterList extends AbstractTermList {

    private final Set<Term> nonBlanks;
    private final Set<Term> optionals;
    private final Map<Term, Term> defaultValues;

    public ParameterList(TermList parameters, Set<Term> nonBlanks, Set<Term> optionals,
        Map<Term, Term> defaultValues) {

        super(parameters);

        this.nonBlanks = (nonBlanks == null) ? new HashSet<>() : nonBlanks;
        this.optionals = (optionals == null) ? new HashSet<>() : optionals;
        this.defaultValues = (defaultValues == null) ? new HashMap<>() : defaultValues;
    }
    
    public ParameterList(List<Term> parameters, Set<Term> nonBlanks, Set<Term> optionals,
        Map<Term, Term> defaultValues) {
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
        return isNonBlank(this.terms.get(index));
    }

    public Set<Term> getNonBlanks() {
        return this.nonBlanks;
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

    public Map<Term, Term> getDefaultValues() {
        return this.defaultValues;
    }

    public boolean isOptional(int index) {
        return isOptional(get(index));
    }

    public boolean isOptional(Term e) {
        return this.optionals.contains(e);
    }

    public Set<Term> getOptional() {
        return this.optionals;
    }

    public ParameterList shallowCloneTerms() {
        List<Term> clonedTerms = new LinkedList<>();
        Set<Term> clonedNonBlanks = new HashSet<>();
        Set<Term> clonedOptionals = new HashSet<>();
        Map<Term, Term> clonedDefaults = new HashMap<>();

        for (Term t : this.terms.asList()) {
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
        String s = "";
        String sep = "";
        for (Term e : asList()) {
            s = s + sep + e.toString(prefixes);
            s += this.optionals.contains(e) ? " : ?" : "";
            sep = ", ";
        }
        return "(" + s + ")";
    }

    @Override
    public String toString() {
        String s = "";
        String sep = "";
        for (Term e : asList()) {
            s = s + sep + e.toString();
            s += this.optionals.contains(e) ? " : ?" : " : ";
            String type = e.getType().toString();
            s += type;
            sep = ", ";
        }
        return "(" + s + ")";
    }

    @Override
    public boolean equals(Object o) {
        return this == o
            || this.getClass() == o.getClass()
            && this.terms.asList().equals(((ParameterList) o).terms.asList())
            && this.defaultValues.equals(((ParameterList) o).defaultValues)
            && this.optionals.equals(((ParameterList) o).optionals);
    }

    @Override
    public int hashCode() {
        return this.terms.asList().hashCode() + 3 * this.defaultValues.hashCode() + 5 * this.optionals.hashCode();
    }
}
