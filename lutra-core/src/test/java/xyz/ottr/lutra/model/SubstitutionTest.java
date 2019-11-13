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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import xyz.ottr.lutra.model.terms.NoneTerm;
import xyz.ottr.lutra.model.terms.ObjectTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.model.terms.TermList;
import xyz.ottr.lutra.system.Result;

public class SubstitutionTest {

    @Test
    public void simpleSubstitution() {
        TermList params = new TermList(
                new ObjectTerm("a", true),
                new ObjectTerm("b", true),
                new ObjectTerm("c", true));

        TermList args = new TermList(
                new ObjectTerm(1),
                new ObjectTerm(2),
                new ObjectTerm(3));

        Result<Substitution> subsRes = Substitution.makeSubstitution(args, params);
        assertTrue(subsRes.isPresent());
        Substitution subs = subsRes.get();

        TermList bodyIns = new TermList(
                new ObjectTerm("c", true),
                new ObjectTerm(4),
                new ObjectTerm("b", true));

        TermList shouldEqual = new TermList(
                new ObjectTerm(3),
                new ObjectTerm(4),
                new ObjectTerm(2));

        assertEquals(subs.apply(bodyIns).asList(), shouldEqual.asList());
    }

    @Test
    public void listSubstitution() {

        TermList params = new TermList(
            new ObjectTerm("a", true),
            new ObjectTerm("b", true),
            new ObjectTerm("c", true));

        TermList arg02 = new TermList(new ObjectTerm(1));
        TermList args = new TermList(arg02, new ObjectTerm(2), arg02);

        Result<Substitution> subsRes = Substitution.makeSubstitution(args, params);
        assertTrue(subsRes.isPresent());
        Substitution subs = subsRes.get();

        Term bodyArg01 = new ObjectTerm("a", true);
        TermList bodyIns = new TermList(bodyArg01, bodyArg01, new ObjectTerm("c", true));

        TermList exp = subs.apply(bodyIns);

        assertEquals(exp.get(0), exp.get(1)); 
        assertEquals(exp.get(1), exp.get(2));
    }

    @Test
    public void defaultValue() {

        Term p1 = new ObjectTerm("a", true);
        Term p1Default = new ObjectTerm(0);
        TermList paramVars = new TermList(
                p1,
                new ObjectTerm("b", true),
                new ObjectTerm("c", true));
        
        Map<Term, Term> defaultVals = new HashMap<>();
        defaultVals.put(p1, p1Default);
        ParameterList params = new ParameterList(paramVars, null, null, defaultVals);

        ArgumentList argsWithoutNone = new ArgumentList(
                new ObjectTerm(1),
                new ObjectTerm(2),
                new ObjectTerm(3));

        ArgumentList argsWithNone = new ArgumentList(
                new NoneTerm(),
                new ObjectTerm(2),
                new ObjectTerm(3));

        Result<Substitution> subsWithNoneRes = Substitution.makeSubstitution(argsWithNone, params);
        assertTrue(subsWithNoneRes.isPresent());
        Substitution subsWithNone = subsWithNoneRes.get();

        Result<Substitution> subsWithoutNoneRes = Substitution.makeSubstitution(argsWithoutNone, params);
        assertTrue(subsWithoutNoneRes.isPresent());
        Substitution subsWithoutNone = subsWithoutNoneRes.get();

        TermList bodyIns = new TermList(
                new ObjectTerm("a", true),
                new ObjectTerm("b", true),
                new ObjectTerm("c", true));

        TermList withNoneshouldEqual = new TermList(
                new ObjectTerm(0),
                new ObjectTerm(2),
                new ObjectTerm(3));

        TermList withoutNoneshouldEqual = new TermList(
                new ObjectTerm(1),
                new ObjectTerm(2),
                new ObjectTerm(3));

        assertEquals(subsWithNone.apply(bodyIns).asList(), withNoneshouldEqual.asList());
        assertEquals(subsWithoutNone.apply(bodyIns).asList(), withoutNoneshouldEqual.asList());
    }

    @Test
    public void wrongConstruction() {

        TermList params = new TermList(
                new ObjectTerm("a", true),
                new ObjectTerm("b", true),
                new ObjectTerm("c", true));

        TermList args = new TermList(
                new ObjectTerm(1),
                new ObjectTerm(2));

        Result<Substitution> subs = Substitution.makeSubstitution(args, params);
        assertFalse(subs.isPresent());
    }
}
