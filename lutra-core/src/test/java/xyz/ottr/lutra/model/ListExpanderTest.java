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

import static org.hamcrest.CoreMatchers.is;
import static xyz.ottr.lutra.model.terms.ObjectTerm.cons;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import xyz.ottr.lutra.model.terms.ListTerm;
import xyz.ottr.lutra.model.terms.NoneTerm;
import xyz.ottr.lutra.model.terms.Term;

public class ListExpanderTest {

    private final List<Argument> args1 = List.of(
        Argument.builder().term(new ListTerm(cons(1), cons(2), cons(3))).listExpander(true).build(),
        Argument.builder().term(new ListTerm(cons(4), cons(5))).listExpander(true).build(),
        Argument.builder().term(cons(6)).build());

    private final List<Argument> args2 = List.of(
        Argument.builder().term(new ListTerm(cons(1), cons(2), cons(3))).listExpander(true).build(),
        Argument.builder().term(new ListTerm(cons(4), cons(5))).listExpander(true).build(),
        Argument.builder().term(new ListTerm()).listExpander(true).build());


    private static List<List<Term>> expandToTerms(List<Argument> arguments, ListExpander expander) {

        List<List<Term>> expanded = expander.expand(arguments).stream()
            .map(list -> list.stream()
                .map(Argument::getTerm)
                .collect(Collectors.toList()))
            .collect(Collectors.toList());
        return expanded;
    }


    private static void test(List<Argument> input, ListExpander expander, List<List<Term>> expected) {
        List<List<Term>> expanded = expandToTerms(input, expander);
        Assert.assertThat(expanded, is(expected));
    }


    @Test
    public void crossTest1() {

        List<List<Term>> shouldEqual = List.of(
            List.of(cons(1), cons(4), cons(6)),
            List.of(cons(1), cons(5), cons(6)),
            List.of(cons(2), cons(4), cons(6)),
            List.of(cons(2), cons(5), cons(6)),
            List.of(cons(3), cons(4), cons(6)),
            List.of(cons(3), cons(5), cons(6)));

        test(this.args1, ListExpander.cross, shouldEqual);
    }

    @Test
    public void zipMinTest1() {

        List<List<Term>> shouldEqual = List.of(
            List.of(cons(1), cons(4), cons(6)),
            List.of(cons(2), cons(5), cons(6)));

        test(this.args1, ListExpander.zipMin, shouldEqual);
    }

    @Test
    public void zipMaxTest1() {

        List<List<Term>> shouldEqual = List.of(
            List.of(cons(1), cons(4), cons(6)),
            List.of(cons(2), cons(5), cons(6)),
            List.of(cons(3), new NoneTerm(), cons(6)));

        test(this.args1, ListExpander.zipMax, shouldEqual);
    }

    @Test
    public void crossTest2Empty() {
        test(this.args2, ListExpander.cross, Collections.<List<Term>>emptyList());
    }

    @Test
    public void zipMinTest2Empty() {
        test(this.args2, ListExpander.zipMin, Collections.<List<Term>>emptyList());
    }

}
