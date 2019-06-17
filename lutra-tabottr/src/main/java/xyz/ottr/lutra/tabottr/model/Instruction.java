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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public abstract class Instruction {

    protected Table table;
    protected int rowStart;
    protected int rowEnd;

    protected Instruction(Table table, int rowStart, int rowEnd) {
        this.table = table;
        this.rowStart = rowStart;
        this.rowEnd = rowEnd;
    }

    /**
     * Returns a pair of integers, the first denotes the index of
     * the table this instruction is in, and the second the integer
     * of the row this instruction starts.
     */
    public int[] getStartCoordinates() {
        return new int[]{this.table.getIndex(), this.rowStart};
    }

    protected List<List<String>> getRows(int rowStart, int rowEnd, int[] colIndices) {
        List<List<String>> rows = new ArrayList<>();
        for (int rowIndex = rowStart; rowIndex <= rowEnd; rowIndex += 1) {
            rows.add(getRow(rowIndex, colIndices));
        }
        return Collections.unmodifiableList(rows);
    }

    protected List<String> getRow(int rowIndex, int[] colIndices) {
        // check that indices are within bounds:
        if (rowIndex < this.rowStart) {
            throw new IndexOutOfBoundsException(); // TODO informative message
        }
        if (rowIndex > this.rowEnd) {
            throw new IndexOutOfBoundsException(); // TODO informative message
        }
        
        List<String> row = new ArrayList<>(colIndices.length);
        for (int colIndex : colIndices) {
            row.add(this.table.getCellValue(rowIndex, colIndex));
        }
        return Collections.unmodifiableList(row);
    }
    
    protected List<String> getRow(int rowIndex) {
        return getRow(rowIndex, IntStream.range(0, this.table.getWidth()).toArray());
    }
}
