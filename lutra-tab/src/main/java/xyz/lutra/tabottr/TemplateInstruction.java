package xyz.lutra.tabottr;

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
		return table.getCellValue(this.rowStart, 2);
	}

	/**
	 * Get the list of template instance argument indices, 
	 * e.g., the n'th value in the array is the column index
	 * of the n'th instance argument.
	 * @return
	 */
	private int[] getColIndices() {
		// get the row of user indices:
		List<String> indexRow = super.getRow(this.rowStart+1);

		// init output array
		int[] colIndices = new int[indexRow.size()];

		// find largest user index
		int maxIndex = 0;
		// find all positive user indices
		for(int i = 0; i < indexRow.size(); i += 1) {
			int index = NumberUtils.toInt(indexRow.get(i), -1); 
			if (index > 0) {
				colIndices[index-1] = i;
				maxIndex = Math.max(maxIndex, index);
			}
		}
		return Arrays.copyOfRange(colIndices, 0, maxIndex);
	}	

	public List<String> getArgumentTypes() {
		return super.getRow(this.rowStart+2, this.colIndices);
	}
	
	public List<String> getArgumentHeadings() {
		return super.getRow(this.rowStart+3, this.colIndices);
	}

	public List<List<String>> getTemplateInstances() {
		return super.getRows(this.rowStart+4, this.rowEnd, this.colIndices);
	}

}
