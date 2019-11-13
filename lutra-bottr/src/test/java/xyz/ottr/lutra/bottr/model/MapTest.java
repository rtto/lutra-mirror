package xyz.ottr.lutra.bottr.model;

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

import static org.hamcrest.CoreMatchers.is;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.shared.PrefixMapping;
import org.junit.Assert;
import org.junit.Test;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.bottr.source.StringArgumentMap;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.types.TypeFactory;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;

public class MapTest {

    @Test
    public void prototypeTest() {

        String ns = "http://example.com/ns#";

        // Input: Set up source with some data
        class StaticTestSource implements Source<String> {

            private final List<List<String>> rows;

            StaticTestSource() {

                this.rows = new ArrayList<>();
                this.rows.add(List.of(ns + "A1", ns + "B1", ns + "C1"));
                this.rows.add(List.of(ns + "A2", ns + "B2", ns + "C2"));
            }

            // NB! Returns same rows regardless of query
            @Override
            public ResultStream<List<String>> execute(String query) {
                return new ResultStream<>(this.rows.stream().map(Result::of));
            }

            @Override
            public ResultStream<ArgumentList> execute(String query, ArgumentMaps<String> argumentMaps) {
                return new ResultStream<>(this.rows.stream()
                    .map(argumentMaps));
            }

            @Override
            public ArgumentMap<String> createArgumentMap(PrefixMapping prefixMapping) {
                return null;
            }
        }

        Source<String> source = new StaticTestSource();
        PrefixMapping prefixes = OTTR.getDefaultPrefixes();
        StringArgumentMap iriMap = new StringArgumentMap(prefixes, TypeFactory.IRI);

        // Set up map to translate source to triple instances
        ArgumentMaps<String> valMap = new ArgumentMaps<>(PrefixMapping.Standard, source,
            List.of(iriMap, iriMap, iriMap)
        );

        InstanceMap<String> myMap = new InstanceMap<>(
            source,
            "blank query",
            OTTR.BaseURI.Triple,
            valMap);

        // Output: "Manually" build two instances      
        Instance inst1 = new Instance(OTTR.BaseURI.Triple,
                new ArgumentList(
                        new IRITerm(ns + "A1"),
                        new IRITerm(ns + "B1"),
                        new IRITerm(ns + "C1")));
        Instance inst2 = new Instance(OTTR.BaseURI.Triple,
                new ArgumentList(
                        new IRITerm(ns + "A2"),
                        new IRITerm(ns + "B2"),
                        new IRITerm(ns + "C2")));
        
        Set<Instance> output = new HashSet<>();
        output.add(inst1);
        output.add(inst2);

        // get input instances
        Set<Instance> input = myMap.get()
            .getStream()
            .filter(Result::isPresent)
            .map(Result::get)
            .collect(Collectors.toSet());
        
        Assert.assertThat(input.size(), is(2));
        Assert.assertThat(input, is(output));
    }

}