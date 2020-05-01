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

import xyz.ottr.lutra.bottr.model.InstanceMap;
import xyz.ottr.lutra.bottr.parser.BInstanceMapParser;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.parser.InstanceParser;
import xyz.ottr.lutra.system.ResultStream;
import xyz.ottr.lutra.wottr.io.RDFIO;

public class BInstanceReader implements InstanceParser<String> {

    @Override
    public ResultStream<Instance> apply(String file) {
        return ResultStream.innerOf(file)
            .innerFlatMap(RDFIO.fileReader())
            .innerFlatMap(new BInstanceMapParser(file))
            .innerFlatMap(InstanceMap::get);
    }

}
