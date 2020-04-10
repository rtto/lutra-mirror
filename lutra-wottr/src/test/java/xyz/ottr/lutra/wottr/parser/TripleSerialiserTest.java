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
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.junit.Test;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.wottr.io.Models;

public class TripleSerialiserTest {

    @Test
    public void test1() {

        Model model = Models.readModel("src/test/resources/div/tripleserialiser/PizzaInstance.ttl");

        WInstanceParser instanceParser = new WInstanceParser();

        var triples = instanceParser.apply(model).getStream()
            .map(Result::get)
            .filter(i -> i.getIri().equals(OTTR.BaseURI.NullableTriple))
            .collect(Collectors.toList());

        assertThat(Collections.emptyList(), is(triples));
    }
}
