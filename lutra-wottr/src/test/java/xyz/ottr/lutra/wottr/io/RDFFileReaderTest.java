package xyz.ottr.lutra.wottr.io;

/*-
 * #%L
 * lutra-wottr
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

import java.util.stream.Stream;

import org.apache.jena.rdf.model.Model;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;

public class RDFFileReaderTest {
    
    private static final String nonExistent = "src/test/resources/thisFileDoesNotExist.ttl";
    private static final String faultyRDF = "src/test/resources/incorrect/faultyRDF.ttl";
    private static final String emptyFile = "src/test/resources/correct/emptyFile.ttl";
    private static RDFReader<String> reader;

    private static final Logger log = LoggerFactory.getLogger(RDFFileReaderTest.class);
    
    @BeforeClass    
    public static void load() {
        reader = RDFReader.fileReader();
    }

    @Test
    public void shouldParse() {
        ResultStream<Model> emptyFileModelStream = reader.apply(emptyFile);
        Result<Stream<Model>> aggr = emptyFileModelStream.aggregate();

        assert aggr.isPresent();

        log.debug(Models.writeModel(aggr.get().findFirst().get()));
    }

    @Test
    public void shouldParseButGiveEmptyResult() {

        ResultStream<Model> nonExistentModelStream = reader.apply(nonExistent);
        Result<Stream<Model>> nonExistsentAggr = nonExistentModelStream.aggregate();
        assert !nonExistsentAggr.isPresent();
        log.debug(nonExistsentAggr.getAllMessages().toString());

        ResultStream<Model> faultyRDFModelStream = reader.apply(faultyRDF);
        Result<Stream<Model>> faultyRDFModelAggr = faultyRDFModelStream.aggregate();
        assert !faultyRDFModelAggr.isPresent();
        log.debug(faultyRDFModelAggr.getAllMessages().toString());
    }

    @AfterClass
    public static void clear() {
        reader = null;
    }
}
