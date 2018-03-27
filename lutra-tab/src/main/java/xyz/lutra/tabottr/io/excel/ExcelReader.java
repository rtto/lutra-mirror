package xyz.lutra.tabottr.io.excel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import xyz.lutra.tabottr.Table;
import xyz.lutra.tabottr.io.AbstractFileTableReader;

public class ExcelReader extends AbstractFileTableReader {
	
	private XSSFWorkbook workbook;

	public ExcelReader(String filename) throws InvalidFormatException, IOException {
		super(filename);
		this.workbook = new XSSFWorkbook(filename);
	}

	public List<Table> getTables() {
		List<Table> tables = new ArrayList<>();
		for (int index = 0; index < workbook.getNumberOfSheets(); index += 1) {
			tables.add(getTable(workbook.getSheetAt(index), index + 1));
		}
		return tables;
	}

	private Table getTable(Sheet sheet, int index) {
		// for evaluating formulas and formatting cell values:
		FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
		DataFormatter formatter = new DataFormatter();

		// initialise empty table:
		int height = getTableHeigth(sheet);
		int width = getTableWidth(sheet);
		Table table = new Table(index, height, width);

		// insert excel cells into table:
		for (int rowNo = 0; rowNo < height; rowNo += 1) {
			Row row = sheet.getRow(rowNo);
			for (int colNo = 0; colNo < getRowWidth(row); colNo += 1) {
				Cell cell = row.getCell(colNo, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);
				String cellValue = formatter.formatCellValue(cell, evaluator);
				table.setCellValue(rowNo, colNo, cellValue);
			}
		}
		return table;
	}

	private int getTableHeigth(Sheet sheet) {
		return sheet.getLastRowNum()+1;
	}

	/**
	 * Find the size of the largest row, i.e, max width of the sheet.
	 * @param sheet
	 * @return
	 */
	private int getTableWidth(Sheet sheet) {
		int maxWidth = 0;
		for (int rowNo = 0; rowNo < sheet.getLastRowNum(); rowNo += 1) {
			Row row = sheet.getRow(rowNo);
			int rowWidth = getRowWidth(row);
			if (rowWidth > maxWidth) {
				maxWidth = rowWidth;
			}
		}
		return maxWidth;
	}
	
	private int getRowWidth(Row row) {
		return row != null ? row.getLastCellNum() : 0;
	}
}
