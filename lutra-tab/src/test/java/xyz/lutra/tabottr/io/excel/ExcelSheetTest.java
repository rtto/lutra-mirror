package xyz.lutra.tabottr.io.excel;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Test;

import xyz.lutra.Expander;
import xyz.lutra.tabottr.Table;
import xyz.lutra.tabottr.io.TableReader;
import xyz.lutra.tabottr.io.excel.ExcelReader;
import xyz.lutra.tabottr.io.rdf.RDFWriter;

public class ExcelSheetTest {
	
	private String ROOT = "src/test/resources/";
	
	@Test public void shouldHandleBlank() throws IOException, InvalidFormatException {
		TableReader parser = new ExcelReader(ROOT + "blank.xlsx");
		List<Table> tables = parser.getTables();
		
		for (Table table : tables) {
			System.out.println(table.toString());
		}
		
		RDFWriter writer = new RDFWriter();
		writer.process(tables);
		Model instances = writer.write();
		//ModelIO.printModel(instances, ModelIO.format.TURTLE);
		
		Model expansion = Expander.expand(instances);
		//ModelIO.printModel(expansion, ModelIO.format.TURTLE);
		assertTrue(true);
	}

}
