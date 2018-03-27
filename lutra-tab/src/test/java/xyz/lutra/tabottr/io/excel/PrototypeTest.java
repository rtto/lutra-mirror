package xyz.lutra.tabottr.io.excel;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Test;

import xyz.lutra.tabottr.Table;
import xyz.lutra.tabottr.io.TableReader;
import xyz.lutra.tabottr.io.excel.ExcelReader;

public class PrototypeTest {
	
	private String ROOT = "src/test/resources/";
	
	@Test public void shouldWork() throws IOException, InvalidFormatException {
		TableReader parser = new ExcelReader(ROOT + "test1.xlsx");
		List<Table> tables = parser.getTables();
		
		/*
		for (Table table : tables) {
			System.out.println(table.toString());
		}
		*/
		assertTrue(true);
	}

}
