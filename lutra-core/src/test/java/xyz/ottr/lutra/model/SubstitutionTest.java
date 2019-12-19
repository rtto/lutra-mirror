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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import xyz.ottr.lutra.model.terms.ListTerm;
import xyz.ottr.lutra.model.terms.NoneTerm;
import xyz.ottr.lutra.model.terms.ObjectTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.system.Result;

public class SubstitutionTest {

    @Test
    public void simpleSubstitution() {
        List<Parameter> params = Parameter.of(
                ObjectTerm.var("a"),
                ObjectTerm.var("b"),
                ObjectTerm.var("c"));

        List<Argument> args = Argument.of(
                ObjectTerm.cons(1),
                ObjectTerm.cons(2),
                ObjectTerm.cons(3));

        Result<Substitution> subsRes = Substitution.resultOf(args, params);
        assertTrue(subsRes.isPresent());
        Substitution subs = subsRes.get();

        List<Term> bodyIns = List.of(
                ObjectTerm.var("c"),
                ObjectTerm.cons(4),
                ObjectTerm.var("b"));

        List<Term> shouldEqual = List.of(
                ObjectTerm.cons(3),
                ObjectTerm.cons(4),
                ObjectTerm.cons(2));

        assertEquals(subs.apply(bodyIns), shouldEqual);
    }

    @Test
    public void listSubstitution() {

        List<Parameter> params = Parameter.of(
            ObjectTerm.var("a"),
            ObjectTerm.var("b"),
            ObjectTerm.var("c"));

        ListTerm arg02 = new ListTerm(ObjectTerm.cons(1));
        List<Argument> args = Argument.of(arg02, ObjectTerm.cons(2), arg02);

        Result<Substitution> subsRes = Substitution.resultOf(args, params);
        assertTrue(subsRes.isPresent());
        Substitution subs = subsRes.get();

        Term bodyArg01 = ObjectTerm.var("a");
        List<Term> bodyIns = List.of(bodyArg01, bodyArg01, ObjectTerm.var("c"));

        List<Term> exp = subs.apply(bodyIns);

        assertEquals(exp.get(0), exp.get(1)); 
        assertEquals(exp.get(1), exp.get(2));
    }

    @Test
    public void defaultValue() {

        List<Parameter> params = List.of(
            Parameter.builder()
                .term(ObjectTerm.var("a"))
                .defaultValue(ObjectTerm.cons(0))
                .build(),
            Parameter.builder().term(ObjectTerm.var("b")).build(),
            Parameter.builder().term(ObjectTerm.var("c")).build()
        );

        List<Argument> argsWithoutNone = Argument.of(
                ObjectTerm.cons(1),
                ObjectTerm.cons(2),
                ObjectTerm.cons(3));

        List<Argument> argsWithNone = Argument.of(
                new NoneTerm(),
                ObjectTerm.cons(2),
                ObjectTerm.cons(3));

        Result<Substitution> subsWithNoneRes = Substitution.resultOf(argsWithNone, params);
        assertTrue(subsWithNoneRes.isPresent());
        Substitution subsWithNone = subsWithNoneRes.get();

        Result<Substitution> subsWithoutNoneRes = Substitution.resultOf(argsWithoutNone, params);
        assertTrue(subsWithoutNoneRes.isPresent());
        Substitution subsWithoutNone = subsWithoutNoneRes.get();

        List<Term> bodyIns = List.of(
                ObjectTerm.var("a"),
                ObjectTerm.var("b"),
                ObjectTerm.var("c"));

        List<Term> withNoneshouldEqual = List.of(
                ObjectTerm.cons(0),
                ObjectTerm.cons(2),
                ObjectTerm.cons(3));

        List<Term> withoutNoneshouldEqual = List.of(
                ObjectTerm.cons(1),
                ObjectTerm.cons(2),
                ObjectTerm.cons(3));

        assertEquals(subsWithNone.apply(bodyIns), withNoneshouldEqual);
        assertEquals(subsWithoutNone.apply(bodyIns), withoutNoneshouldEqual);
    }

    @Test
    public void wrongConstruction() {

        List<Parameter> params = Parameter.of(
                ObjectTerm.var("a"),
                ObjectTerm.var("b"),
                ObjectTerm.var("c"));

        List<Argument> args = Argument.of(
                ObjectTerm.cons(1),
                ObjectTerm.cons(2));

        Result<Substitution> subs = Substitution.resultOf(args, params);
        assertFalse(subs.isPresent());
    }
}
