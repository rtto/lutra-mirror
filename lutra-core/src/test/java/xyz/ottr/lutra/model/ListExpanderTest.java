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
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import xyz.ottr.lutra.model.terms.NoneTerm;
import xyz.ottr.lutra.model.terms.ObjectTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.model.terms.TermList;

public class ListExpanderTest {

    @Test
    public void crossTest() {
        Term a1 = new TermList(new ObjectTerm(1), new ObjectTerm(2), new ObjectTerm(3));
        Term a2 = new TermList(new ObjectTerm(4), new ObjectTerm(5));
        TermList argTerms = new TermList(a1, a2, new ObjectTerm(6));
        Set<Term> expanderValues = new HashSet<>();
        expanderValues.add(a1);
        expanderValues.add(a2);
        ArgumentList args = new ArgumentList(argTerms, expanderValues, ArgumentList.Expander.CROSS);

        List<ArgumentList> expanded = args.expandListExpander();
        assertTrue(expanded.size() == 6);

        Set<List<Term>> resultLists = expanded.stream().map(ArgumentList::asList).collect(Collectors.toSet());
        
        Set<List<ObjectTerm>> shouldEqual = Stream.of(
                Stream.of(new ObjectTerm(1), new ObjectTerm(4), new ObjectTerm(6)).collect(Collectors.toList()),
                Stream.of(new ObjectTerm(1), new ObjectTerm(5), new ObjectTerm(6)).collect(Collectors.toList()),
                Stream.of(new ObjectTerm(2), new ObjectTerm(4), new ObjectTerm(6)).collect(Collectors.toList()),
                Stream.of(new ObjectTerm(2), new ObjectTerm(5), new ObjectTerm(6)).collect(Collectors.toList()),
                Stream.of(new ObjectTerm(3), new ObjectTerm(4), new ObjectTerm(6)).collect(Collectors.toList()),
                Stream.of(new ObjectTerm(3), new ObjectTerm(5), new ObjectTerm(6)).collect(Collectors.toList())
            ).collect(Collectors.toSet());

        assertEquals(resultLists, shouldEqual);
    }

    @Test
    public void zipMinTest() {
        Term a1 = new TermList(new ObjectTerm(1), new ObjectTerm(2), new ObjectTerm(3));
        Term a2 = new TermList(new ObjectTerm(4), new ObjectTerm(5));
        TermList argTerms = new TermList(a1, a2, new ObjectTerm(6));
        Set<Term> expanderValues = new HashSet<>();
        expanderValues.add(a1);
        expanderValues.add(a2);
        ArgumentList args = new ArgumentList(argTerms, expanderValues, ArgumentList.Expander.ZIPMIN);

        List<ArgumentList> expanded = args.expandListExpander();
        assertTrue(expanded.size() == 2);

        Set<List<Term>> resultLists = expanded.stream().map(ArgumentList::asList).collect(Collectors.toSet());
        
        Set<List<ObjectTerm>> shouldEqual = Stream.of(
                Stream.of(new ObjectTerm(1), new ObjectTerm(4), new ObjectTerm(6)).collect(Collectors.toList()),
                Stream.of(new ObjectTerm(2), new ObjectTerm(5), new ObjectTerm(6)).collect(Collectors.toList())
            ).collect(Collectors.toSet());

        assertEquals(resultLists, shouldEqual);
    }

    @Test
    public void zipMaxTest() {
        Term a1 = new TermList(new ObjectTerm(1), new ObjectTerm(2), new ObjectTerm(3));
        Term a2 = new TermList(new ObjectTerm(4), new ObjectTerm(5));
        TermList argTerms = new TermList(a1, a2, new ObjectTerm(6));
        Set<Term> expanderValues = new HashSet<>();
        expanderValues.add(a1);
        expanderValues.add(a2);
        ArgumentList args = new ArgumentList(argTerms, expanderValues, ArgumentList.Expander.ZIPMAX);

        List<ArgumentList> expanded = args.expandListExpander();
        assertTrue(expanded.size() == 3);

        Set<List<Term>> resultLists = expanded.stream().map(ArgumentList::asList).collect(Collectors.toSet());
        
        Set<List<? extends Term>> shouldEqual = Stream.of(
                Stream.of(new ObjectTerm(1), new ObjectTerm(4), new ObjectTerm(6)).collect(Collectors.toList()),
                Stream.of(new ObjectTerm(2), new ObjectTerm(5), new ObjectTerm(6)).collect(Collectors.toList()),
                Stream.of(new ObjectTerm(3), new NoneTerm(), new ObjectTerm(6)).collect(Collectors.toList())
            ).collect(Collectors.toSet());

        assertEquals(resultLists, shouldEqual);
    }
}
