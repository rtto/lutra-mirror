package xyz.ottr.lutra.model.terms;

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

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.jena.shared.PrefixMapping;

import xyz.ottr.lutra.model.types.LUBType;
import xyz.ottr.lutra.model.types.ListType;
import xyz.ottr.lutra.model.types.NEListType;
import xyz.ottr.lutra.model.types.TermType;
import xyz.ottr.lutra.model.types.TypeRegistry;

public class TermList extends Term implements SimpleList<Term> {

    private static long newID = 0L;

    private final List<Term> terms;
    private final long listID; // Used to distinguish different lists but with same elements

    public TermList(List<Term> terms, boolean isVariable) {
        super(getTermType(terms), isVariable);
        this.terms = terms;
        this.listID = generateNewID();
    }

    private static TermType getTermType(List<Term> terms) {
        return terms.isEmpty()
            ? new ListType(TypeRegistry.BOT)
            : new NEListType(new LUBType(TypeRegistry.TOP));
    }

    public TermList(List<Term> terms) {
        this(terms, false);
    }

    public TermList(Term... terms) {
        this(List.of(terms));
    }

    /**
     * As variables have a type depending on its declaration in the head
     * of a template, they might not have the proper type set on construction.
     * Thus, the type computed at construction of this TermList might also be
     * incorrect. This method simply recomputes its type, and is called
     * after proper typing of variables in Template.
     */
    public void recomputeType() {

        for (Term inner : this.terms) {
            if (inner instanceof TermList) {
                ((TermList) inner).recomputeType();
            }
        }
        setType(getTermType(this.terms));
    }

    private static long generateNewID() {
        newID += 1;
        return newID;
    }

    public List<Term> asList() {
        return this.terms;
    }

    public boolean equalContentAs(TermList o) {
        return this.asList().equals(o.asList());
    }

    @Override
    public boolean isBlank() {
        return false;
    }

    @Override
    public TermList shallowClone() {
        return new TermList(this.terms, isVariable());
    }

    @Override
    public Optional<Term> unify(Term other) {

        if (!(other instanceof TermList)) {
            return Optional.empty();
        }

        if (isVariable()) {
            return Optional.of(other);
        }

        List<Term> othersList = ((TermList) other).asList();
        if (this.terms.size() != othersList.size()
            || other.isVariable()) {
            return Optional.empty();
        }

        List<Term> result = new LinkedList<>();

        for (int i = 0; i < this.terms.size(); i++) {
            Optional<Term> ot = this.terms.get(i).unify(othersList.get(i));
            if (ot.isEmpty()) {
                return Optional.empty();
            }
            result.add(ot.get());
        }
        return Optional.of(new TermList(result, false));
    }

    @Override
    public Object getIdentifier() {
        return this.listID;
    }
    
    @Override
    public String toString(PrefixMapping prefixes) {
        return this.terms.stream()
            .map(t -> t.toString(prefixes))
            .collect(Collectors.joining(", ", "<", ">"))
            + "(id: " + this.listID + ")";
    }

}
