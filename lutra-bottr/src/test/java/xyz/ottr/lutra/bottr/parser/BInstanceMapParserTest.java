package xyz.ottr.lutra.bottr.parser;

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

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import xyz.ottr.lutra.bottr.model.InstanceMap;
import xyz.ottr.lutra.io.Files;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;
import xyz.ottr.lutra.wottr.io.RDFFileReader;


@RunWith(Parameterized.class)
public class BInstanceMapParserTest {

    private static final String ROOT = "src/test/resources/maps/";
    private static final String[] EMPTY_ARRAY = {};

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<String[]> data() {
        return Files.getFolderContents(ROOT, new String[]{ "ttl" }, EMPTY_ARRAY)
            .innerMap(File::toString)
            .innerMap(string -> new String[] { string })
            .aggregate()
            .map(s -> s.collect(Collectors.toList()))
            .get();
    }

    private final String file;

    public BInstanceMapParserTest(String file) {
        this.file = file;
    }

    @Test
    public void shouldParseWithoutError() {
        Result<List<InstanceMap>> map = getInstanceMaps(this.file);
        Assert.assertThat(map.getAllMessages(), is(Collections.emptyList()));
    }

    private Result<List<InstanceMap>> getInstanceMaps(String file) {
        return ResultStream.innerOf(file)
            .innerFlatMap(new RDFFileReader())
            .innerFlatMap(new BInstanceMapParser(file))
            .aggregate()
            .map(stream -> stream.collect(Collectors.toList()));
    }

}
