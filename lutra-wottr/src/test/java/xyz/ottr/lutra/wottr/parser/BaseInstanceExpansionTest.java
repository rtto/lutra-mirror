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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.ottr.lutra.wottr.io.RDFIO;

public class BaseInstanceExpansionTest {

    public static Stream<Arguments> data() {
        Path root = Paths.get("src",  "test", "resources", "baseinstances");
        return Stream.of(
                arguments(root.resolve("test1-in.ttl").toAbsolutePath().toString(),
                        root.resolve("test1-out.ttl").toAbsolutePath().toString())
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    public void shouldBeIsomorphic(String input, String output) {

        var parseOutput = RDFIO.fileReader().parse(output).get();

        assertNotNull(parseOutput);

        ModelUtils.testIsomorphicModels(
            ModelUtils.getOTTRParsedRDFModel(input),
            parseOutput);
    }
}
