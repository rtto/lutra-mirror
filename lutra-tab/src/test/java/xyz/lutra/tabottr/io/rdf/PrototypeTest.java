package xyz.lutra.tabottr.io.rdf;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Test;

import osl.util.rdf.ModelIO;
import osl.util.rdf.ModelIOException;
import xyz.lutra.Expander;
import xyz.lutra.tabottr.Table;
import xyz.lutra.tabottr.io.TableReader;
import xyz.lutra.tabottr.io.excel.ExcelReader;
import xyz.lutra.tabottr.io.rdf.RDFWriter;

public class PrototypeTest {
	
	private String ROOT = "src/test/resources/";
			
	@Test public void shouldWork() throws IOException, ModelIOException, InvalidFormatException {
		TableReader parser = new ExcelReader(ROOT + "test1.xlsx");
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