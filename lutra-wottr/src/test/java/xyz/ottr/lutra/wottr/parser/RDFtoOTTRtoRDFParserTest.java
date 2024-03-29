package xyz.ottr.lutra.wottr.parser;

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

import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.ottr.lutra.wottr.io.RDFIO;

public class RDFtoOTTRtoRDFParserTest {

    public static Stream<Arguments> data() throws IOException {
        Path folder = Paths.get("src",  "test", "resources", "w3c-rdf-tests");

        return Files.walk(folder)
                .filter(Files::isRegularFile)
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .sorted()
                .map(Arguments::arguments);
    }

    @ParameterizedTest
    @MethodSource("data")
    public void test(String filename) {

        // exclude test files which are bad by design
        assumeFalse(filename.contains("-bad-"));
        assumeFalse(filename.contains("error"));

        // Try parse file with Jena.
        var rdfModel = RDFIO.fileReader().parse(filename);

        // exclude remaining test files that are not accepted by the RDF parser.
        assumeTrue(rdfModel.isPresent());

        Model ottrModel = ModelUtils.getOTTRParsedRDFModel(filename);

        ModelUtils.testIsomorphicModels(ottrModel, rdfModel.get());
    }
}
