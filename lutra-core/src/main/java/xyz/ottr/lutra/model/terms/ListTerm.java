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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Singular;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.model.Substitution;
import xyz.ottr.lutra.model.types.LUBType;
import xyz.ottr.lutra.model.types.ListType;
import xyz.ottr.lutra.model.types.NEListType;
import xyz.ottr.lutra.model.types.Type;
import xyz.ottr.lutra.model.types.TypeRegistry;

public class ListTerm extends AbstractTerm<Long> {

    private static long newID = 0L;

    private final List<Term> terms;
    private final long listID; // Used to distinguish different lists but with same elements

    @Builder(toBuilder = true)
    public ListTerm(@Singular List<Term> terms, boolean variable) {
        super(generateNewID(), getIntrinsicType(terms)); // TODO change this?
        this.terms = terms;
        this.listID = generateNewID();
        this.variable = variable;
    }

    public ListTerm(List<Term> terms) {
        this(terms, false);
    }

    public ListTerm(Term... terms) {
        this(List.of(terms));
    }

    private static Type getIntrinsicType(List<Term> terms) {
        return terms.isEmpty()
            ? new ListType(TypeRegistry.BOT)
            : new NEListType(new LUBType(TypeRegistry.TOP));
    }

    /**
     * As variables have a type depending on its declaration in the head
     * of a template, they might not have the proper type set on construction.
     * Thus, the type computed at construction of this ListTerm might also be
     * incorrect. This method simply recomputes its type, and is called
     * after proper typing of variables in Template.
     */
    public void recomputeType() {
        for (Term inner : this.terms) {
            if (inner instanceof ListTerm) {
                ((ListTerm) inner).recomputeType();
            }
        }
        setType(getIntrinsicType(this.terms));
    }

    private static long generateNewID() {
        newID += 1;
        return newID;
    }

    public List<Term> asList() {
        return Collections.unmodifiableList(this.terms);
    }

    public boolean equalContentAs(ListTerm o) {
        return this.asList().equals(o.asList());
    }

    @Override
    public ListTerm shallowClone() {
        return this.toBuilder().build();
    }

    @Override
    public Term apply(Substitution substitution) {
        return this.toBuilder()
            .clearTerms()
            .terms(substitution.apply(this.terms))
            .build();
    }

    @Override
    public Optional<Term> unify(Term other) {

        if (!(other instanceof ListTerm)) {
            return Optional.empty();
        }

        if (isVariable()) {
            return Optional.of(other);
        }

        List<Term> othersList = ((ListTerm) other).asList();
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
        return Optional.of(new ListTerm(result, false));
    }

    @Override
    public String toString(PrefixMapping prefixes) {
        return this.terms.stream()
            .map(t -> t.toString(prefixes))
            .collect(Collectors.joining(", ", "<", ">"))
            + "(id: " + this.listID + ")";
    }

}
