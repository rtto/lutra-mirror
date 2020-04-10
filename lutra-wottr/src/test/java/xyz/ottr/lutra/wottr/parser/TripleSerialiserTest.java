package xyz.ottr.lutra.wottr.parser;

/*-
 * #%L
 * lutra-wottr
 * %%
 * Copyright (C) 2018 - 2020 University of Oslo
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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.junit.Test;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.wottr.io.Models;

public class TripleSerialiserTest {

    private static final String ROOT = "src/test/resources/tripleserialiser/";

    private Map<String, List<Instance>> getInstanceGrouping(String filename) {

        Model model = Models.readModel(ROOT + filename);

        WInstanceParser instanceParser = new WInstanceParser();

        return instanceParser.apply(model).getStream()
            .map(Result::get)
            .collect(Collectors.groupingBy(Instance::getIri));
    }

    @Test
    public void testPizzaInstance() {
        var instances = getInstanceGrouping("PizzaInstance.ttl");

        assertThat(instances.get(OTTR.BaseURI.NullableTriple), is(nullValue()));
        assertThat(instances.get(OTTR.BaseURI.Triple), is(nullValue()));
        assertThat(instances.get("http://tpl.ottr.xyz/pizza/0.1/NamedPizza").size(), is(1));
    }

    @Test
    public void testPizzaInstance2() {
        var instances = getInstanceGrouping("PizzaInstance2.ttl");

        assertThat(instances.get(OTTR.BaseURI.NullableTriple).size(), is(3));
        assertThat(instances.get(OTTR.BaseURI.Triple), is(nullValue()));
        assertThat(instances.get("http://tpl.ottr.xyz/pizza/0.1/NamedPizza").size(), is(3));
    }

    @Test
    public void testPizzaInstance3() {
        var instances = getInstanceGrouping("PizzaInstance3.ttl");

        assertThat(instances.get(OTTR.BaseURI.NullableTriple).size(), is(21));
        assertThat(instances.get(OTTR.BaseURI.Triple), is(nullValue()));
        assertThat(instances.get("http://tpl.ottr.xyz/pizza/0.1/NamedPizza").size(), is(3));
    }
}
