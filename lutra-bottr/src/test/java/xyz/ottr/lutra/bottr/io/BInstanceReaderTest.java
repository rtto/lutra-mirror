package xyz.ottr.lutra.bottr.io;

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

import java.util.stream.Stream;
import org.apache.jena.rdf.model.Model;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultConsumer;
import xyz.ottr.lutra.wottr.io.RDFIO;
import xyz.ottr.lutra.wottr.writer.WInstanceWriter;

public class BInstanceReaderTest {

    private static final String ROOT = "src/test/resources/";

    private void testNumberOfInstances(String map, long size) {
        Stream<Result<Instance>> instances = Result.of(map)
            .mapToStream(new BInstanceReader())
            .getStream()
            .filter(Result::isPresent);

        Assert.assertThat(instances.count(), is(size));
    }

    private Model getRDFModel(String mapFile) {

        WInstanceWriter writer = new WInstanceWriter();
        ResultConsumer<Instance> consumer = new ResultConsumer<>(writer);

        Result.of(mapFile)
            .mapToStream(new BInstanceReader())
            .forEach(consumer);

        //System.out.println(consumer.getMessageHandler().getMessages());

        return writer.writeToModel();
    }

    private void printRDFOutput(String file) {
        Model model = getRDFModel(file);
        String output = RDFIO.writeToString(model);
        //System.out.println(output);
    }

    @Test
    @Ignore("Uses external SPARQL endpoint. Problematic to rely on outside sources for unit tests.")
    public void testSPARQLMap() {
        String file = ROOT + "maps/instanceMapSPARQL.ttl";
        testNumberOfInstances(file, 13L);
        printRDFOutput(file);
    }

    @Test
    public void testRDFSourceMap() {
        String file = ROOT + "maps/instanceMapRDFSource.ttl";
        testNumberOfInstances(file,6L);
        printRDFOutput(file);
    }

    @Ignore
    @Test
    public void testCSVSourceMap() {
        String file = ROOT + "maps/instanceMapH2Source.ttl";
        printRDFOutput(file);
        testNumberOfInstances(file, 5L);
    }

    @Test
    public void testCSVSourceMapLists() {
        String file = ROOT + "maps/listInstanceMapH2Source.ttl";
        printRDFOutput(file);
        testNumberOfInstances(file, 1L);
    }

    @Test
    public void testRDFSourceMapLists() {
        String file = ROOT + "maps/listInstanceMapRDFSource.ttl";
        printRDFOutput(file);
        testNumberOfInstances(file, 1L);
    }



}
