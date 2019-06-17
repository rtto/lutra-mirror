package xyz.ottr.lutra.tabottr.model;

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

import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PrefixInstruction extends Instruction {

    private static final int[] colIndices = { 0, 1 };
    
    public PrefixInstruction(Table table, int rowStart, int rowEnd) {
        super(table, rowStart, rowEnd);
    }

    /**
     * Returns a list of pairs of Strings, the first
     * entry in each pair being the prefix and the second the namespace. A list
     * of pairs is chosen over a map, as this allows all prefix conflicts to be detected
     * in a single parse, which TableParser is responsible for detecting.
     */
    public List<Map.Entry<String,String>> getPrefixPairs() {

        List<Map.Entry<String,String>> prefixes = new LinkedList<>();

        for (List<String> pair : super.getRows(this.rowStart + 1, this.rowEnd, this.colIndices)) {
            String prefix = pair.get(0);
            String ns = pair.get(1);
            prefixes.add(new AbstractMap.SimpleEntry(prefix, ns));
        }
        return prefixes;
    }
}
