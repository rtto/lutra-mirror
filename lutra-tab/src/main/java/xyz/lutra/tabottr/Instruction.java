package xyz.lutra.tabottr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public abstract class Instruction {

	protected Table table;
	protected int rowStart;
	protected int rowEnd;

	public Instruction(Table table, int rowStart, int rowEnd) {
		this.table = table;
		this.rowStart = rowStart;
		this.rowEnd = rowEnd;
	}

	protected List<List<String>> getRows (int rowStart, int rowEnd, int[] colIndices) {
		List<List<String>> rows = new ArrayList<>();
		for (int rowIndex = rowStart; rowIndex <= rowEnd; rowIndex += 1) {
			rows.add(getRow(rowIndex, colIndices));
		}
		return Collections.unmodifiableList(rows);
	}

	protected List<String> getRow (int rowIndex, int[] colIndices) {
		// check that indices are within bounds:
		if (rowIndex < this.rowStart) {
			throw new IndexOutOfBoundsException(); // TODO informative message
		}
		if (rowIndex > this.rowEnd) {
			throw new IndexOutOfBoundsException(); // TODO informative message
		}
		
		List<String> row = new ArrayList<>(colIndices.length);
		for (int colIndex : colIndices) {
			row.add(table.getCellValue(rowIndex, colIndex));
		}
		return Collections.unmodifiableList(row);
	}
	
	protected List<String> getRow (int rowIndex) {
		return getRow(rowIndex, IntStream.range(0, table.getWidth()).toArray());
	}

}