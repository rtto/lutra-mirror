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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.NoneTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.model.terms.TermList;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;

public class Substitution {

    private final Map<Term, Term> termSubstitution; // Represents substitution of simple terms
    
    public Substitution(Map<Term, Term> termSubstitution) {
        this.termSubstitution = new HashMap<>();
        this.termSubstitution.putAll(termSubstitution);
    }

    public Substitution() {
        this(new HashMap<>());
    }

    private Map<Term, Term> getTermSubstition() {
        return this.termSubstitution;
    }

    public static Result<Substitution> makeSubstitution(TermList args, TermList parameters) {
        return makeSubstitution(new ArgumentList(args.asList()), new ParameterList(parameters.asList()));
    }

    public static Result<Substitution> makeSubstitution(ArgumentList args, ParameterList parameters) {

        if (args.asList().size() != parameters.asList().size()) {
            return Result.empty(Message.error(
                        "Cannot make substitution out of two term lists"
                        + " with different lengths: " + args.toString()
                        + " and " + parameters.toString()));
        }
                        

        Map<Term, Term> termSubstitution = new HashMap<>();
        for (int i = 0; i < args.size(); i++) {
            if (args.get(i) instanceof NoneTerm && parameters.hasDefaultValue(i)) {
                Term dflt = parameters.getDefaultValue(i);
                if (dflt instanceof BlankNodeTerm) { // Blank node default results in fresh blank node
                    termSubstitution.put(parameters.get(i), new BlankNodeTerm());
                } else {
                    termSubstitution.put(parameters.get(i), dflt);
                }
            } else {
                termSubstitution.put(parameters.get(i), args.get(i));
            }
        }
        return Result.of(new Substitution(termSubstitution));
    }

    /**
     * Applies this substitution to the argument TermList, and
     * constructs fresh blank nodes for lists if argument keepListIDs
     * is false, and keeps the original blank nodes if it is true.
     */
    public TermList apply(TermList args) {
        List<Term> substituted = new ArrayList<>();
        for (Term p : args.asList()) {
            if (p instanceof TermList) {
                TermList tl = (TermList) p;
                substituted.add(apply(tl));
            } else if (p.isBlank() && !this.termSubstitution.containsKey(p)) {
                BlankNodeTerm blank = new BlankNodeTerm();
                this.termSubstitution.put(p, blank);
                substituted.add(blank);
            } else {
                substituted.add(this.termSubstitution.getOrDefault(p, p));
            }
        }
        return new TermList(substituted, args.isVariable());
    }

    /**
     * Applies this substitution to the argument ParameterList, and
     * constructs fresh blank nodes for lists if argument keepListIDs
     * is false, and keeps the original blank nodes if it is true.
     */
    public ArgumentList apply(ArgumentList args) {
        TermList substituted = apply(args.getTermList());

        Set<Term> newExpanderValues = null;
        if (args.getExpanderValues() != null) {
            newExpanderValues = args.getExpanderValues().stream()
                .map(t -> this.termSubstitution.getOrDefault(t, t))
                .collect(Collectors.toCollection(HashSet::new));
        }
        return new ArgumentList(substituted, newExpanderValues, args.getListExpander());
    }

    /**
     * Applies this substitution to the argument Instance,
     * constructing new blank nodes for lists.
     */
    public Instance apply(Instance instance) {
        return new Instance(instance.getIRI(), apply(instance.getArguments()));
    }

    /**
     * Applies this substitution to the argument template body,
     * constructing new blank nodes for lists.
     */
    public Set<Instance> apply(Set<Instance> body) {
        return body.stream().map(this::apply).collect(Collectors.toSet());
    }
    
    /**
     * Creates a new Substitution which represents the merge of this with argument
     * if a merge is possible, and returns empty if not.
     */
    public Optional<Substitution> mergeWithUnification(Substitution other) {

        Map<Term, Term> newTermSubs = new HashMap<>(this.termSubstitution);
        Map<Term, Term> otherSubs = other.getTermSubstition();
        for (Map.Entry<Term, Term> e : otherSubs.entrySet()) {
            if (!newTermSubs.containsKey(e.getKey())) {
                newTermSubs.put(e.getKey(), e.getValue());
            } else {
                // TODO: Should not(?) unifiy the two values, only succeed if equal
                //Optional<Term> unified = newTermSubs.get(e.getKey()).unify(e.getValue()); 
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

    public Term get(Term from) {
        return this.termSubstitution.get(from);
    }

    @Override
    public String toString() {
        return "<Term substitution: " + this.termSubstitution.toString() + ">";
    }
}
