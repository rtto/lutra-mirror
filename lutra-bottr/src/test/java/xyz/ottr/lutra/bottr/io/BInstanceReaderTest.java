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

import static org.junit.Assert.assertEquals;

import java.util.stream.Stream;

import org.apache.jena.rdf.model.Model;
import org.junit.Test;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultConsumer;
import xyz.ottr.lutra.wottr.util.ModelIO;
import xyz.ottr.lutra.wottr.writer.v04.WInstanceWriter;


public class BInstanceReaderTest {

    private static final String ROOT = "src/test/resources/";

    private void testNumberOfInstances(String map, int size) {
        Stream<Result<Instance>> instances = Result.of(map)
            .mapToStream(new BInstanceReader())
            .getStream()
            .filter(Result::isPresent);

        assertEquals(size, instances.count());
    }

    private Model getRDFModel(String mapFile) {

        WInstanceWriter writer = new WInstanceWriter();
        ResultConsumer<Instance> consumer = new ResultConsumer<>(writer);

        Result.of(mapFile)
            .mapToStream(new BInstanceReader())
            .forEach(consumer);

        return writer.writeToModel();
    }

    private void printRDFOutput(String file) {
        Model model = getRDFModel(file);
        String output = ModelIO.writeModel(model);
        //System.out.println(output);
    }

    @Test
    public void testSPARQLMap() {
        String file = ROOT + "maps/instanceMapSPARQL.ttl";
        testNumberOfInstances(file, 13);
        printRDFOutput(file);
    }

    @Test
    public void testRDFSourceMap() {
        String file = ROOT + "maps/instanceMapRDFSource.ttl";
        testNumberOfInstances(file,6);
        printRDFOutput(file);
    }

    @Test
    public void testCSVSourceMap() {
        String file = ROOT + "maps/instanceMapH2Source.ttl";
        testNumberOfInstances(file, 14);
        printRDFOutput(file);
    }



}
