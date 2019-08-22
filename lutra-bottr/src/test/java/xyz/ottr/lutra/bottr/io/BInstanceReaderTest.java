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

import org.junit.Test;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.result.Result;

public class BInstanceReaderTest {

    private static final String ROOT = "src/test/resources/";

    @Test
    public void testSPARQLMap() {

        Stream<Result<Instance>> instances = Result.of(ROOT + "maps/instanceMapSPARQL.ttl")
            .mapToStream(new BInstanceReader())
            .getStream();

        assertEquals(13, instances.count());
    }

    @Test
    public void testRDFSourceMap() {

        Stream<Result<Instance>> instances = Result.of(ROOT + "maps/instanceMapRDFSource.ttl")
            .mapToStream(new BInstanceReader())
            .getStream();

        assertEquals(6, instances.count());
    }

    @Test
    public void testCSVSourceMap() {

        Stream<Result<Instance>> instances = Result.of(ROOT + "maps/instanceMapH2Source.ttl")
            .mapToStream(new BInstanceReader())
            .getStream();

        assertEquals(7, instances.count());
    }

}
