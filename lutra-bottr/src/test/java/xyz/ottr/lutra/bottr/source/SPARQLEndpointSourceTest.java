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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Collectors;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import xyz.ottr.lutra.bottr.model.Source;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;

@Isolated
public class SPARQLEndpointSourceTest {

    @Test
    @Disabled("Fails sometimes due to 'HttpConnectTimeoutException: HTTP connect timed out'.")
    public void prototypeTest() {
        String endpoint = "http://dbpedia.org/sparql";
        Source<RDFNode> source = new SPARQLEndpointSource(endpoint);
        
        ResultStream<?> result = source.execute("SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 13");
        MatcherAssert.assertThat(result.getStream().count(), is(13L));
    }

    @Test
    public void emptyQueryResult() {
        String expectedString = "no results";
        String queryString = "SELECT ?s ?p ?o WHERE {?s ?p ?o} LIMIT 0";

        Model model = createModel();
        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(query, model);

        SPARQLEndpointSource source = mock(SPARQLEndpointSource.class);
        when(source.getQueryExecution(queryString)).thenReturn(Result.of(qe));
        when(source.execute(queryString)).thenCallRealMethod();

        ResultStream<?> resultStream = source.execute(queryString);
        Result<?> emptyResult = resultStream.getStream().collect(Collectors.toList()).get(0);
        Assertions.containsMessageFragment(emptyResult.getMessageHandler(), Message.Severity.INFO, expectedString);
    }

    public void addStatement(Model model, String s, String p, String o) {
        Resource subject = model.createResource(s);
        Property predicate = model.createProperty(p);
        RDFNode object = model.createResource(o);
        Statement stmt = model.createStatement(subject, predicate, object);
        model.add(stmt);
    }

    public Model createModel() {
        Model model = ModelFactory.createDefaultModel();
        String ns = "http://www.example.org#";
        String nsRDFS = "http://www.w3.org/2000/01/rdf-schema#";

        addStatement(model, ns + "Fish", nsRDFS + "subClassOf", ns + "Animal");
        addStatement(model, ns + "Fish", ns + "livesIn", ns + "Water");
        addStatement(model, ns + "Mammal", nsRDFS + "subClassOf", ns + "Animal");
        addStatement(model, ns + "Mammal", ns + "has", ns + "Vertebra");
        addStatement(model, ns + "Whale", nsRDFS + "subClassOf", ns + "Mammal");
        addStatement(model, ns + "Whale", ns + "livesIn", ns + "Water");
        addStatement(model, ns + "Cat", nsRDFS + "subClassOf", ns + "Mammal");
        addStatement(model, ns + "Cat", ns + "has", ns + "Fur");
        addStatement(model, ns + "Bear", nsRDFS + "subClassOf", ns + "Mammal");
        addStatement(model, ns + "Bear", ns + "has", ns + "Fur");

        return model;
    }
}
