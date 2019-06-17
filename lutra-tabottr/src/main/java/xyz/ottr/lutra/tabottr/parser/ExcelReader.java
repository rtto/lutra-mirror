package xyz.ottr.lutra.tabottr.parser;

import java.io.File;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import xyz.ottr.lutra.io.InstanceParser;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.tabottr.model.Table;

public class ExcelReader implements InstanceParser<String> {

    @Override
    public ResultStream<Instance> apply(String filename) {
        return ExcelReader.parseTables(filename).mapToStream(TableParser::processInstructions);
    }
    
    /**
     * Parses the spreadsheet at given filename into a list
     * of Table-objects wrapped in Result on success, and
     * empty Result with error message if error occurs.
     *
     * @param filename
     *      The name of the file to parse
     *
     * @return A Result either containing a List of Tables or
     *      empty with error messages on error
     */
    public static Result<List<Table>> parseTables(String filename) {
        // TODO Rather take a File as input, and handle possible file IO issues somewhere more "generic"?
        File file = new File(filename);
        // open file in read-only mode and without any password:
        try (Workbook workbook = WorkbookFactory.create(file, null, true)) {
            List<Table> tables = new ArrayList<>();
            for (int index = 0; index < workbook.getNumberOfSheets(); index += 1) {
                tables.add(parseTable(workbook.getSheetAt(index), index + 1));
            }
            return Result.of(tables);
        } catch (IOException ex) {
            Message msg = Message.error(ex.getMessage());
            return Result.empty(msg);
        } catch (InvalidOperationException ex) {
            Message msg = Message.error(ex.getMessage());
            return Result.empty(msg);
        } catch (NotOfficeXmlFileException ex) {
            Message msg = Message.error(ex.getMessage());
            return Result.empty(msg);
        } catch (EncryptedDocumentException ex) {
            Message msg = Message.error(ex.getMessage());
            return Result.empty(msg);
        }
    }

    private static Table parseTable(Sheet sheet, int index) {
        // for evaluating formulas and formatting cell values:
        FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
        evaluator.setIgnoreMissingWorkbooks(true);
        
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

    private static int getTableHeigth(Sheet sheet) {
        return sheet.getLastRowNum() + 1;
    }

    /**
     * Find the size of the largest row, i.e, max width of the sheet.
     * @param sheet
     * @return
     */
    private static int getTableWidth(Sheet sheet) {
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
    
    private static int getRowWidth(Row row) {
        return row != null ? row.getLastCellNum() : 0;
    }
}
