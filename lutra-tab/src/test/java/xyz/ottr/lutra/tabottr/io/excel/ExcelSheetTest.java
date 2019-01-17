package xyz.ottr.lutra.tabottr.io.excel;

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

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.tabottr.io.TabInstanceParser;

public class ExcelSheetTest {
    
    private static final String ROOT = "src/test/resources/";
    
    @Test
    public void shouldHandleBlank() {
        TabInstanceParser parser = new TabInstanceParser();
        ResultStream<Instance> instances = parser.apply(ROOT + "blank.xlsx");
        instances.forEach(ins -> System.out.println(ins.toString()));

        assertTrue(true);
    }

}
