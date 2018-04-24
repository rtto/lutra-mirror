package xyz.lutra.tabottr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Table {

	private String[][] data;
	private int index;
	private String rowNumberFormat;

	public Table(int index, int heigth, int width) {
		this.index = index;
		this.data = new String[heigth][width];
		this.rowNumberFormat = "%0" + String.valueOf(heigth).length() + "d";
	}
	
	public int getHeight() {
		return data.length;
	}
	
	public int getWidth() {
		return data.length != 0 ? data[0].length : 0;
	}
	
	/**
	 * All values are trimmed on insertion.
	 * @param row
	 * @param col
	 * @param value
	 */
	public void setCellValue(int row, int col, String value) {
		data[row][col] = value.trim();
	}
	
	public String getCellValue(int row, int col) {
		return data[row][col];
	}

	public String toString() {
		StringBuilder str = new StringBuilder();

		// counter for row numbering:
		int rowNo = 1;

		for (String[] row : data) {
			str
			.append(getFormattedRowNumber(rowNo++))
			.append(": ")
			.append(Arrays.toString(row))
			.append("\n");
		}
		return str.toString();
	}

	public List<Instruction> getInstructions() {
		
		List<Instruction> instructions = new ArrayList<>();

		// collect row numbers containing OTTR token:
		List<Integer> tokenIndices = new ArrayList<>();
		for (int rowIndex = 0; rowIndex < data.length; rowIndex += 1) {
			if (data[rowIndex].length > 1 && TabOTTR.TOKEN.equals(data[rowIndex][0])) {
				tokenIndices.add(rowIndex);
			}
		}

		// split table into instruction according to OTTR tokens indices:
		for (int i = 0; i < tokenIndices.size(); i += 1) {
			int start = tokenIndices.get(i);
			String name = data[start][1];

			// ignore end instructions
			if (TabOTTR.INSTRUCTION_END.equals(name)) {
				continue;
			}

			// find last instruction row -- either last table row or row before next instruction:
			int end = (i+1 == tokenIndices.size()) ? data.length-1 : tokenIndices.get(i+1)-1;
			
			instructions.add(createInstruction(name, start, end));
		}
		return instructions;
	}
	
	private Instruction createInstruction(String name, int start, int end) {
		switch (name) {
		case TabOTTR.INSTRUCTION_TEMPLATE:
			return new TemplateInstruction(this, start, end);
		case TabOTTR.INSTRUCTION_PREFIX:
			return new PrefixInstruction(this, start, end);
		default:
			throw new IllegalArgumentException(
					"Unreckognised instruction: " + name + " at " + getFormattedRowNumber(start));
		}
	}

	private String getFormattedRowNumber (int rowNo) {
		return index + "." + String.format(this.rowNumberFormat, rowNo);
	}

}
