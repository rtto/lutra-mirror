package xyz.ottr.lutra.tabottr;

/*-
 * #%L
 * lutra-tabottr
 * %%
 * Copyright (C) 2018 - 2020 University of Oslo
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

import xyz.ottr.lutra.io.Format;
import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.tabottr.parser.ExcelReader;

public class TabottrFormat implements Format {
    
    private final InstanceReader instanceReader;
    
    public TabottrFormat() {
        this.instanceReader = new InstanceReader(new ExcelReader());
    }

    @Override
    public Result<InstanceReader> getInstanceReader() {
        return Result.of(this.instanceReader);
    }

    @Override
    public boolean supports(Operation op, ObjectType ot) {
        return op == Operation.read && ot == ObjectType.instance; 
    }

    @Override
    public String getDefaultFileSuffix() {
        return ".xlsx";
    }

    @Override
    public String getFormatName() {
        return "TabOTTR";
    }
}
