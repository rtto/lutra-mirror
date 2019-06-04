package xyz.ottr.lutra.tabottr.io;

/*-
 * #%L
 * lutra-tab
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

import xyz.ottr.lutra.io.InstanceParser;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.tabottr.io.excel.ExcelReader;

// TODO extend to other file types such as CSV when we support reading these formats.
public class TabInstanceParser implements InstanceParser<String> {

    @Override
    public ResultStream<Instance> apply(String filename) {
        return ExcelReader.parseTables(filename).mapToStream(TableParser::processInstructions);
    }
}
