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

import static org.junit.Assert.assertNotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import xyz.ottr.lutra.wottr.io.RDFReader;

@RunWith(Parameterized.class)
public class BaseInstanceExpansionTest {

    @Parameterized.Parameters(name = "{index}: {0}, {1}")
    public static List<String[]> data() {

        List<String[]> data = new ArrayList<>();

        Path root = Paths.get("src",  "test", "resources", "baseinstances");

        data.add(new String[]{ root.resolve("test1-in.ttl").toString(), root.resolve("test1-out.ttl").toString() });
        return data;
    }

    private final String input;
    private final String output;

    public BaseInstanceExpansionTest(String input, String output) {
        this.input = input;
        this.output = output;
    }

    @Test
    public void shouldBeIsomorphic() {

        var output = RDFReader.fileReader().parse(this.output).get();

        assertNotNull(output);

        ModelUtils.testIsomorphicModels(
            ModelUtils.getOTTRParsedRDFModel(this.input),
            output);
    }
}
