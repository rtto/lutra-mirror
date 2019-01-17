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

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import org.apache.jena.shared.PrefixMapping;

public class ArgumentList {

    public enum Expander { CROSS, ZIPMIN, ZIPMAX }

    private final Set<Term> expanderValues;
    private final Expander listExpander;
    private final TermList terms;

    public ArgumentList(TermList parameters, Set<Term> expanderValues, Expander listExpander) {
        this.terms = parameters;
        this.listExpander = listExpander;
        this.expanderValues = (expanderValues == null) ? new HashSet<>() : expanderValues;
    }
    
    public ArgumentList(List<Term> parameters, Set<Term> expanderValues, Expander listExpander) {
        this(new TermList(parameters), expanderValues, listExpander);
    }
    
    public ArgumentList(List<Term> parameters) {
        this(parameters, null, null);
    }
    
    public ArgumentList(Term... elems) {
        this(Arrays.asList(elems), null, null);
    }

    public Expander getListExpander() {
        return this.listExpander;
    }

    public boolean hasListExpander() {
        return this.listExpander != null;
    }

    public boolean hasListExpander(int index) {
        return hasListExpander(get(index));
    }

    public boolean hasListExpander(Term e) {
        return this.expanderValues.contains(e);
    }

    public boolean hasCrossExpander() {
        return this.listExpander != null && this.listExpander.equals(Expander.CROSS);
    }

    public boolean hasZipExpander() {
        return hasZipMinExpander() || hasZipMaxExpander();
    }

    public boolean hasZipMinExpander() {
        return this.listExpander != null && this.listExpander.equals(Expander.ZIPMIN);
    }

    public boolean hasZipMaxExpander() {
        return this.listExpander != null && this.listExpander.equals(Expander.ZIPMAX);
    }

    public Set<Term> getExpanderValues() {
        return this.expanderValues;
    }

    public TermList getTermList() {
        return this.terms;
    }

    public List<Term> asList() {
        return this.terms.asList();
    }
    
    public int size() {
        return this.terms.size();
    }

    public boolean isEmpty() {
        return this.terms.isEmpty();
    }

    public Term get(int i) {
        return this.terms.get(i);
    }

    public ArgumentList shallowCloneTerms() {
        List<Term> clonedTerms = new LinkedList<>();
        Set<Term> clonedExpanderValues = new HashSet<>();

        for (Term t : this.terms.asList()) {
            Term nt = t.shallowClone();
            clonedTerms.add(nt);
            if (this.expanderValues.contains(t)) {
                clonedExpanderValues.add(nt);
            }
        }
        return new ArgumentList(clonedTerms, clonedExpanderValues, this.listExpander);
    }

    // TODO Use StringBuilder?
    /**
     * Returns a String similar to toString(), but
     * IRIs are written as qnames according to the
     * argument PrefixMapping.
     */
    public String toString(PrefixMapping prefixes) {
        String s = "";
        String sep = "";
        for (Term e : asList()) {
            s = s.concat(sep + e.toString(prefixes));
            //s = s.concat(this.expanderValues != null && this.expanderValues.contains(e) ? "--e" : "");
            sep = ", ";
        }
        return "(" + s + ")";
    }

    @Override
    // TODO Use StringBuilder?
    public String toString() {
        String s = "";
        String sep = "";
        for (Term e : asList()) {
            s = s.concat(sep + e.toString());
            //s = s.concat(this.expanderValues != null && this.expanderValues.contains(e) ? "--e" : "");
            sep = ", ";
        }
        return "(" + s + ")";
    }

    private void expandCrossExpander(List<ArgumentList> expanded, List<Term> current, int i) {
        if (i >= asList().size()) {
            expanded.add(new ArgumentList(current));
            return;
        }

        Term t = get(i);
        if (hasListExpander(t)) {
            for (Term e : ((TermList) t).asList()) {
                List<Term> currentC = new LinkedList<>(current);
                currentC.add(e);
                expandCrossExpander(expanded, currentC, i + 1);
            }
        } else {
            current.add(t);
            expandCrossExpander(expanded, current, i + 1);
        }
    }

    private void expandZipExpander(List<ArgumentList> expanded) {

        IntStream lens = this.expanderValues.stream().mapToInt(trm -> ((TermList) trm).size());
        int len = hasZipMinExpander() ? lens.min().getAsInt() : lens.max().getAsInt();
        for (int i = 0; i < len; i++) {
            expanded.add(zipExpandAtIndex(i));
        }
    }

    /**
     * Picks out i'th zip by picking out i'th argument of all lists with
     * expander set, but NoneTerms for lists with expander but shorter than
     * i.
     */
    private ArgumentList zipExpandAtIndex(int i) {

        List<Term> picked = new LinkedList<>();
        for (Term arg : this.terms.asList()) {
            if (hasListExpander(arg)) {
                TermList lst = (TermList) arg;
                if (lst.size() <= i) { // ZipMax and shorter than longest list
                    picked.add(new NoneTerm());
                } else {
                    picked.add(lst.get(i));
                }
            } else {
                picked.add(arg);
            }
        }
        return new ArgumentList(picked);
    }

    public List<ArgumentList> expandListExpander() {
        List<ArgumentList> expanded = new LinkedList<>();
        if (hasCrossExpander() && !this.expanderValues.isEmpty()) {
            expandCrossExpander(expanded, new LinkedList<Term>(), 0);
        } else if (hasZipExpander() && !this.expanderValues.isEmpty()) {
            expandZipExpander(expanded);
        } else {  
            expanded.add(this);
        }
        return expanded;
    }

    @Override
    public boolean equals(Object o) {
        return this == o
            || this.getClass() == o.getClass()
            && this.terms.asList().equals(((ArgumentList) o).terms.asList())
            && this.expanderValues.equals(((ArgumentList) o).expanderValues);
    }

    @Override
    public int hashCode() {
        return this.terms.asList().hashCode() + 3 * this.expanderValues.hashCode();
    }
}
