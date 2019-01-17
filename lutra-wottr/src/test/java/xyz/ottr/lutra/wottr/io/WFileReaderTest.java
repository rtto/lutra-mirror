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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Model;
import org.dyreriket.gaupa.rdf.ModelIO;
import org.dyreriket.gaupa.rdf.ModelIOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.wottr.io.WFileReader;

public class WFileReaderTest {

    private static final String nonExistent = "src/test/resources/thisFileDoesNotExist.ttl";
    private static final String faultyRDF = "src/test/resources/incorrect/faultyRDF.ttl";
    private static final String emptyFile = "src/test/resources/correct/emptyFile.ttl";
    private static WFileReader reader;

    @BeforeClass    
    public static void load() {
        reader = new WFileReader();
    }

    @Test
    public void shouldParse() throws ModelIOException {
        ResultStream<Model> emptyFileModelStream = reader.apply(emptyFile);
        Result<Stream<Model>> aggr = emptyFileModelStream.aggregate();

        assert aggr.isPresent();

        System.out.println(ModelIO.writeModel(aggr.get().findFirst().get(), ModelIO.Format.TURTLE));
    }

    @Test
    public void shouldParseButGiveEmptyResult() {

        ResultStream<Model> nonExistentModelStream = reader.apply(nonExistent);
        Result<Stream<Model>> nonExistsentAggr = nonExistentModelStream.aggregate();
        assert !nonExistsentAggr.isPresent();
        System.out.println(nonExistsentAggr.getMessages().toString());

        ResultStream<Model> faultyRDFModelStream = reader.apply(faultyRDF);
        Result<Stream<Model>> faultyRDFModelAggr = faultyRDFModelStream.aggregate();
        assert !faultyRDFModelAggr.isPresent();
        System.out.println(faultyRDFModelAggr.getMessages().toString());
    }

    @AfterClass
    public static void clear() {
        reader = null;
    }
}
