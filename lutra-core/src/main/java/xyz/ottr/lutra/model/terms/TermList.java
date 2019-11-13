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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.model.types.TypeFactory;

public class TermList extends Term {

    private static int newID = 0;

    private final List<Term> terms;
    private final int listID; // Used to distinguish diferent lists but with same elements

    public TermList(List<Term> terms, boolean isVariable) {
        this.terms = terms;
        this.listID = getNewID();
        super.setIsVariable(isVariable);
        super.type = TypeFactory.getConstantType(this);
    }

    public TermList(List<Term> terms) {
        this(terms, false);
    }

    public TermList(Term... terms) {
        this(Arrays.asList(terms));
    }

    /**
     * As variables have a type depending on its declaration in the head
     * of a template, they might not have the proper type set on construction.
     * Thus, the type computed at construction of this TermList might also be
     * incorrect. This method simply recomputes its type, and is called
     * after proper typing of variables in Template.
     */
    public void recomputeType() {

        for (Term inner : terms) {
            if (inner instanceof TermList) {
                ((TermList) inner).recomputeType();
            }
        }
        this.type = TypeFactory.getConstantType(this);
    }

    private static int getNewID() {
        newID++;
        return newID;
    }

    public List<Term> asList() {
        return this.terms;
    }
    
    public int size() {
        return terms.size();
    }

    public boolean isEmpty() {
        return terms.isEmpty();
    }

    public Term get(int i) {
        return terms.get(i);
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
        TermList t = new TermList(this.terms, this.isVariable);
        t.setIsVariable(super.isVariable());
        return t;
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
            if (!ot.isPresent()) {
                return Optional.empty();
            }
            result.add(ot.get());
        }
        return Optional.of(new TermList(result, false));
    }

    @Override
    public Object getIdentifier() {
        return listID;
    }
    
    @Override
    public String toString(PrefixMapping prefixes) {
        StringBuilder s = new StringBuilder();
        String sep = "";
        for (Term e : this.terms) {
            s.append(sep + e.toString(prefixes));
            sep = ",";
        }
        return "<" + s.toString() + ">" + ">(" + listID + ")";
    }

    @Override
    public String toString() {
        return "<" + StringUtils.join(this.terms, ", ") + ">(" + listID + ")";
    }
}
