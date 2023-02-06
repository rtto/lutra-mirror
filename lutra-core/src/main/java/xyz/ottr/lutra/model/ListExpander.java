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

import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;
import xyz.ottr.lutra.model.terms.ListTerm;
import xyz.ottr.lutra.model.terms.NoneTerm;
import xyz.ottr.lutra.model.terms.Term;

public enum ListExpander {

    cross {
        @Override
        public List<List<Argument>> expand(List<Argument> arguments) {

            List<List<Argument>> expanded = new LinkedList<>();
            expanded.add(new LinkedList<>());

            for (Argument arg : arguments) {
                // add arg to all lists in expanded, via newexpanded.
                List<List<Argument>> newexpanded = new LinkedList<>();
                for (List<Argument> list : expanded) {
                    if (arg.isListExpander()) {

                        if (arg.getTerm() instanceof NoneTerm) {
                            Argument none = Argument.builder().term(new NoneTerm()).build();
                            list.add(none);
                            newexpanded.add(list);
                        } else {
                            // add *all elements* of list term to all list in expanded.
                            for (Term term : ((ListTerm) arg.getTerm()).asList()) {
                                Argument termArg = Argument.builder().term(term).build();
                                List<Argument> newlist = new LinkedList<>(list);
                                newlist.add(termArg);
                                newexpanded.add(newlist);
                            }
                        }
                    } else {
                        list.add(arg);
                        newexpanded.add(list);
                    }
                }
                expanded = newexpanded;
            }
            return expanded;
        }
    },

    zipMin {
        @Override
        public List<List<Argument>> expand(List<Argument> arguments) {
            return zip(arguments, ListExpander.getListTermExpanderSizes(arguments).min().orElse(0));
        }
    },

    zipMax {
        @Override
        public List<List<Argument>> expand(List<Argument> arguments) {
            return zip(arguments, ListExpander.getListTermExpanderSizes(arguments).max().orElse(0));
        }
    };

    public abstract List<List<Argument>> expand(List<Argument> arguments);


    /**
     * Get the all list sizes of the (ListTerm) arguments that are marked for list expansion.
     * Used for finding the shortest or longest list.
     * @param arguments
     * @return stream of list sizes
     */
    private static IntStream getListTermExpanderSizes(List<Argument> arguments) {
        return arguments.stream()
            .filter(Argument::isListExpander)
            .mapToInt(a -> a.getTerm() instanceof NoneTerm
                        ? 1
                        : ((ListTerm) a.getTerm()).asList().size());
    }

    /**
     * Make the zip of the arguments, all listExpander list are set to size zipLength, where
     * smaller lists are extended with None.
     */
    private static List<List<Argument>> zip(List<Argument> arguments, int zipLength) {

        var expanded = new LinkedList<List<Argument>>();
        for (int pick = 0; pick < zipLength; pick += 1) {
            List<Argument> zipStep = new LinkedList<>();
            for (Argument arg : arguments) {
                if (arg.isListExpander()) {
                    if (arg.getTerm() instanceof NoneTerm) {
                        Argument none = Argument.builder().term(new NoneTerm()).build();
                        zipStep.add(none);
                    } else {
                        List<Term> argTerms = ((ListTerm) arg.getTerm()).asList();
                        // Use None if the list is not long enough, only applies for zipMax.
                        Term newTerm = argTerms.size() <= pick
                            ? new NoneTerm()
                            : argTerms.get(pick);
                        zipStep.add(Argument.builder().term(newTerm).build());
                    }
                } else {
                    zipStep.add(arg);
                }
            }
            expanded.add(zipStep);
        }
        return expanded;
    }

}
