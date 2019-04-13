package xyz.ottr.lutra.bottr.source;

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

import org.junit.Test;

import xyz.ottr.lutra.bottr.model.Row;
import xyz.ottr.lutra.bottr.model.Source;
import xyz.ottr.lutra.result.ResultStream;

public class SPARQLEndpointSourceTest {

    @Test
    public void prototypeTest() {
        String endpoint = "https://query.wikidata.org/bigdata/namespace/wdq/sparql";
        Source source = new SPARQLEndpointSource(endpoint);
        
        ResultStream<Row> result = source.execute("SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 13");
        
        assertEquals(13, result.getStream().count());
    }

}
