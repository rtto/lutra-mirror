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
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.ToString;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.NoneTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.system.Result;

@ToString
public class Substitution {

    private final Map<Term, Term> termSubstitution; // Represents substitution of simple terms
    
    public Substitution(Map<Term, Term> termSubstitution) {
        this.termSubstitution = new HashMap<>(termSubstitution);
    }

    public Substitution() {
        this(new HashMap<>());
    }

    public static Result<Substitution> resultOf(List<Argument> args, List<Parameter> parameters) {

        if (args.size() != parameters.size()) {
            return Result.error("Cannot create substitution out of two term lists with different lengths: "
                + args + " and " + parameters);
        }

        Map<Term, Term> termSubstitution = new HashMap<>();
        for (int i = 0; i < args.size(); i++) {
            Term argument = args.get(i).getTerm();
            if (argument instanceof NoneTerm && parameters.get(i).hasDefaultValue()) {
                Term defaultValue = parameters.get(i).getDefaultValue();
                // reassign argument with default value
                // Blank node default results in fresh blank node
                argument = defaultValue instanceof BlankNodeTerm
                    ? new BlankNodeTerm()
                    : defaultValue;
            }
            termSubstitution.put(parameters.get(i).getTerm(), argument);
        }
        return Result.of(new Substitution(termSubstitution));
    }

    public <E> List<E> apply(List<? extends HasApplySubstitution<E>> list) {
        return list.stream()
            .map(element -> element.apply(this))
            .collect(Collectors.toList());
    }

    public <E> Set<E> apply(Set<? extends HasApplySubstitution<E>> list) {
        return list.stream()
            .map(element -> element.apply(this))
            .collect(Collectors.toSet());
    }

    /**
     * Creates a new Substitution which represents the merge of this with argument
     * if a merge is possible, and returns empty if not.
     */
    public Optional<Substitution> mergeWithUnification(Substitution other) {

        Map<Term, Term> newTermSubs = new HashMap<>(this.termSubstitution);
        Map<Term, Term> otherSubs = new HashMap<>(other.termSubstitution);
        for (Map.Entry<Term, Term> e : otherSubs.entrySet()) {
            if (!newTermSubs.containsKey(e.getKey())) {
                newTermSubs.put(e.getKey(), e.getValue());
            } else {
                // TODO: Should not(?) unifiy the two values, only succeed if equal
                //Optional<Term> unified = newTermSubs.get(e.getKey()).unify(e.getLiteral());
                //if (!unified.isPresent()) {
                //    return Optional.empty();
                //}
                //newTermSubs.put(e.getKey(), unified.get());
                if (!newTermSubs.get(e.getKey()).equals(e.getValue())) {
                    return Optional.empty();
                }
            }
        }

        return Optional.of(new Substitution(newTermSubs));
    }

    public Term get(Term term) {
        return this.termSubstitution.get(term);
    }

    public Term getOrCompute(Term term, Function<Term, Term> function) {
        return this.termSubstitution.computeIfAbsent(term, function);
    }

}
