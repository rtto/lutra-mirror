package xyz.ottr.lutra.bottr.parser;

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

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.stream.Collectors;

import org.junit.Test;
import xyz.ottr.lutra.bottr.model.InstanceMap;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.wottr.io.RDFFileReader;

public class BInstanceMapParserTest {

    private static final String ROOT = "src/test/resources/maps/";

    @Test
    public void shouldParseSQL() {

        Result<InstanceMap> maps = getInstanceMap(ROOT + "instanceMapDummyJDBC.ttl");

        assertEquals(Collections.emptyList(), maps.getAllMessages());
        assertEquals("SELECT name, age, company FROM TABLE tblEmployee", maps.get().getQuery());
        assertEquals("http://example.com/tpl#MyTemplate", maps.get().getTemplateIRI());
    }

    @Test
    public void shouldParseSPARQL() {

        Result<InstanceMap> maps = getInstanceMap(ROOT + "instanceMapSPARQL.ttl");

        assertEquals(Collections.emptyList(), maps.getAllMessages());
    }

    @Test
    public void shouldParseRDF() {

        Result<InstanceMap> maps = getInstanceMap(ROOT + "instanceMapRDFSource.ttl");

        assertEquals(Collections.emptyList(), maps.getAllMessages());
    }

    @Test
    public void shouldParseCSV() {

        Result<InstanceMap> maps = getInstanceMap(ROOT + "instanceMapH2Source.ttl");

        assertEquals(Collections.emptyList(), maps.getAllMessages());
    }

    private Result<InstanceMap> getInstanceMap(String file) {
        return ResultStream.innerOf(file)
            .innerFlatMap(new RDFFileReader())
            .innerFlatMap(new BInstanceMapParser())
            .getStream()
            //.peek(r -> System.out.println(r))
            //.peek(r -> System.out.println(r.getAllMessages()))
            //.map(Result::get)
            .collect(Collectors.toList())
            .get(0);
    }

}
