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

import static org.hamcrest.CoreMatchers.is;

import org.apache.jena.rdf.model.RDFNode;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import xyz.ottr.lutra.bottr.model.Source;
import xyz.ottr.lutra.system.ResultStream;

public class SPARQLEndpointSourceTest {

    @Test
    @Disabled("Fails sometimes due to 'HttpConnectTimeoutException: HTTP connect timed out'.")
    public void prototypeTest() {
        String endpoint = "http://dbpedia.org/sparql";
        Source<RDFNode> source = new SPARQLEndpointSource(endpoint);
        
        ResultStream<?> result = source.execute("SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 13");
        MatcherAssert.assertThat(result.getStream().count(), is(13L));
    }
}
