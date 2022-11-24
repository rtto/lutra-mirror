package xyz.ottr.lutra.tabottr.parser;


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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.parser.InstanceParser;
import xyz.ottr.lutra.system.Result;

public class ExcelSheetTest {

    private static final Path ROOT = Paths.get("src", "test", "resources");
    
    @Test
    public void shouldHandleEmptySheets() {
        InstanceParser<String> parser = new ExcelReader();
        List<Result<Instance>> result = parser.apply(ROOT.resolve("blank.xlsx").toString()).collect(Collectors.toList());
        assertEquals(0, result.size());
    }

}
