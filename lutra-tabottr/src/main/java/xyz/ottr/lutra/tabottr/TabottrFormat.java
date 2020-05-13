package xyz.ottr.lutra.tabottr;

import java.util.Collection;
import java.util.Set;

import org.apache.jena.shared.PrefixMapping;

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
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.tabottr.parser.ExcelReader;

public class TabottrFormat implements Format {

    private static final String name = "tabOTTR";
    private static final Collection<Support> support = Set.of(Support.InstanceReader);
    
    private final InstanceReader instanceReader;
    
    public TabottrFormat() {
        this.instanceReader = new InstanceReader(new ExcelReader());
    }

    @Override
    public Result<InstanceReader> getInstanceReader() {
        return Result.of(this.instanceReader);
    }

    @Override
    public Collection<Support> getSupport() {
        return support;
    }

    @Override
    public String getFormatName() {
        return name;
    }

    @Override
    public void setPrefixMapping(PrefixMapping prefixes) {
        // Does not need a PrefixMapping
    }
}
