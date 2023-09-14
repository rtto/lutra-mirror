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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.RDFNode;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import xyz.ottr.lutra.bottr.model.Source;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;

public class RDFFileSourceTest {

    private final Path root = Paths.get("src", "test", "resources", "sources", "rdf");

    private String getResourceFile(String file) {
        return this.root.resolve(file).toString();
    }

    @Test
    @Disabled
    public void prototype() {

        List<String> modelURIs = List.of(getResourceFile("a.ttl"), getResourceFile("b.ttl"));

        Source<RDFNode> source = new RDFFileSource(modelURIs);

        ResultStream<?> result = source.execute(
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>  "
                        + "SELECT ?s WHERE { ?s a foaf:Person }");
        MatcherAssert.assertThat(result.getStream().count(), is(6L));
    }

    @Test
    @Disabled
    public void emptyQueryResult() {
        String expectedString = "no results";
        List<String> modelURIs = List.of(getResourceFile("a.ttl"), getResourceFile("b.ttl"));
        Source<RDFNode> source = new RDFFileSource(modelURIs);

        ResultStream<?> resultStream = source.execute("SELECT ?s ?p ?o { ?s ?p ?o } LIMIT 0");
        Result<?> emptyResult = resultStream.getStream().collect(Collectors.toList()).get(0);
        Assertions.containsMessageFragment(emptyResult.getMessageHandler(), Message.Severity.INFO, expectedString);
    }
}
