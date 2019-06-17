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

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.math.NumberUtils;

public class TemplateInstruction extends Instruction {

    //private static Logger log = LoggerFactory.getLogger(TemplateInstruction.class);

    private int[] colIndices;

    public TemplateInstruction(Table table, int rowStart, int rowEnd) {
        super(table, rowStart, rowEnd);
        this.colIndices = getColIndices();
    }

    public String getTemplateIRI() {
        return this.table.getCellValue(this.rowStart, 2);
    }

    /**
     * Get the list of template instance argument indices, 
     * e.g., the n'th value in the array is the column index
     * of the n'th instance argument.
     * @return
     */
    private int[] getColIndices() {
        // get the row of user indices:
        List<String> indexRow = super.getRow(this.rowStart + 1);

        // init output array
        int[] colIndices = new int[indexRow.size()];

        // find largest user index
        int maxIndex = 0;
        // find all positive user indices
        for (int i = 0; i < indexRow.size(); i += 1) {
            int index = NumberUtils.toInt(indexRow.get(i), -1); 
            if (index > 0) {
                colIndices[index - 1] = i;
                maxIndex = Math.max(maxIndex, index);
            }
        }
        return Arrays.copyOfRange(colIndices, 0, maxIndex);
    }    

    public List<String> getArgumentTypes() {
        return super.getRow(this.rowStart + 2, this.colIndices);
    }
    
    public List<String> getArgumentHeadings() {
        return super.getRow(this.rowStart + 3, this.colIndices);
    }

    public List<List<String>> getTemplateInstanceRows() {
        return super.getRows(this.rowStart + 4, this.rowEnd, this.colIndices);
    }

}
