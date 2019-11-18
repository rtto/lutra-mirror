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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.Getter;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.terms.NoneTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.model.terms.TermList;

@Getter
public class ArgumentList extends AbstractTermList {

    public enum Expander { CROSS, ZIPMIN, ZIPMAX }

    private final Set<Term> expanderValues; // TODO: Bug, must be based on indices
    private final Expander listExpander;

    public ArgumentList(TermList parameters, Set<Term> expanderValues, Expander listExpander) {
        super(parameters);
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
        this(List.of(elems), null, null);
    }

    // TODO: replace all expander tests with a single Optional<Expander> getExpander?

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
        return Expander.CROSS == this.listExpander;
    }

    private boolean hasZipExpander() {
        return hasZipMinExpander() || hasZipMaxExpander();
    }

    public boolean hasZipMinExpander() {
        return Expander.ZIPMIN == this.listExpander;
    }

    public boolean hasZipMaxExpander() {
        return Expander.ZIPMAX == this.listExpander;
    }

    public ArgumentList shallowCloneTerms() {
        List<Term> clonedTerms = new LinkedList<>();
        Set<Term> clonedExpanderValues = new HashSet<>();

        for (Term t : this.termList.asList()) {
            Term nt = t.shallowClone();
            clonedTerms.add(nt);
            if (this.expanderValues.contains(t)) {
                clonedExpanderValues.add(nt);
            }
        }
        return new ArgumentList(clonedTerms, clonedExpanderValues, this.listExpander);
    }

    public String toString(PrefixMapping prefixes) {
        return asList().stream()
            .map(t -> t.toString(prefixes))
            .collect(Collectors.joining(", ", "(", ")"));
    }

    @Override
    public String toString() {
        return toString(OTTR.getDefaultPrefixes());
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
        for (Term arg : this.termList.asList()) {
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
            expandCrossExpander(expanded, new LinkedList<>(), 0);
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
            && this.termList.asList().equals(((ArgumentList) o).termList.asList())
            && this.expanderValues.equals(((ArgumentList) o).expanderValues);
    }

    @Override
    public int hashCode() {
        return this.termList.asList().hashCode() + 3 * this.expanderValues.hashCode();
    }
}
