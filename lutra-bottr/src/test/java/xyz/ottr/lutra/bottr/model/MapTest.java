package xyz.ottr.lutra.bottr.model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.shared.PrefixMapping;
import org.junit.Test;

import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.bottr.model.InstanceMap;
import xyz.ottr.lutra.bottr.model.Source;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.IRITerm;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.tabottr.TabOTTR;

/*-
 * #%L
 * lutra-bottr
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

public class MapTest {

    @Test
    public void prototypeTest() {

        final String ns = "http://example.com/ns#";

        // Input: Set up source with some data
        class StaticTestSource implements Source {

            private List<Row> rows;

            public StaticTestSource() {
                this.rows = new ArrayList<>();
                this.rows.add(new Row(Arrays.asList(ns + "A1", ns + "B1", ns + "C1")));
                this.rows.add(new Row(Arrays.asList(ns + "A2", ns + "B2", ns + "C2")));
            }

            // NB! Returns same rows regardless of query
            @Override
            public ResultStream<Row> execute(String query) {
                return new ResultStream<Row>(rows.stream().map(Result::of));
            }
        }

        // Set up map to translate source to triple instances
        ValueMap valMap = new ValueMap(PrefixMapping.Standard, Arrays.asList(TabOTTR.TYPE_IRI, TabOTTR.TYPE_IRI, TabOTTR.TYPE_IRI));

        InstanceMap myMap = new InstanceMap(
                new StaticTestSource(), 
                "blank query", 
                OTTR.Bases.Triple, 
                valMap);

        // Output: "Manually" build two instances      
        Instance inst1 = new Instance(OTTR.Bases.Triple, 
                new ArgumentList(
                        new IRITerm(ns + "A1"),
                        new IRITerm(ns + "B1"),
                        new IRITerm(ns + "C1")));
        Instance inst2 = new Instance(OTTR.Bases.Triple, 
                new ArgumentList(
                        new IRITerm(ns + "A2"),
                        new IRITerm(ns + "B2"),
                        new IRITerm(ns + "C2")));
        
        Set<Instance> output = new HashSet<>();
        output.add(inst1);
        output.add(inst2);

        // get input instances
        Set<Instance> input = myMap.get().innerCollect(Collectors.toSet());
        
        assertEquals(2, input.size());
        assertEquals(input, output);
    }

}