package xyz.lutra.tabottr.io.rdf;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Test;

import osl.util.rdf.ModelIO;
import osl.util.rdf.ModelIOException;
import xyz.lutra.tabottr.Table;
import xyz.lutra.tabottr.io.TableReader;
import xyz.lutra.tabottr.io.excel.ExcelReader;
import xyz.lutra.tabottr.io.rdf.RDFWriter;

public class RDFWriterTest {
	
	private final String ROOT = "src/test/resources/";
	
	private Model getExcelReaderRDFWriterModel(String filename) throws InvalidFormatException, IOException {
		TableReader parser = new ExcelReader(filename);
		List<Table> tables = parser.getTables();
		RDFWriter writer = new RDFWriter();
		writer.process(tables);
		Model model = writer.write();
		return model;
	}
	
	private void runAtomicTest (String name) throws InvalidFormatException, IOException, ModelIOException {
		String folder = ROOT + "atomic/";
		String inFile = folder + name + ".xlsx";
		String outFile = folder + name + ".ttl";
		Model in = getExcelReaderRDFWriterModel(inFile);
		Model out = ModelIO.readModel(outFile);
		boolean isIsomorphic = in.isIsomorphicWith(out);
		// print if error:
		if (!isIsomorphic) {
			System.out.println("Error: inFile " + inFile + " not isomorphic to outFile " + outFile);
			ModelIO.printModel(in, ModelIO.format.TURTLE);
			ModelIO.printModel(out, ModelIO.format.TURTLE);
		}
		assertTrue(isIsomorphic);
	}
	
	@Test public void testTypedBooleans() throws InvalidFormatException, IOException, ModelIOException {
		runAtomicTest("typedBooleans");
	}
	
	@Test public void testTypedInts() throws InvalidFormatException, IOException, ModelIOException {
		runAtomicTest("typedInts");
	}
	
	@Test public void testTypedIntergers() throws InvalidFormatException, IOException, ModelIOException {
		runAtomicTest("typedIntegers");
	}
	
	@Test public void testTypedDecimals() throws InvalidFormatException, IOException, ModelIOException {
		runAtomicTest("typedDecimals");
	}
	
	@Test public void testTypedStrings() throws InvalidFormatException, IOException, ModelIOException {
		runAtomicTest("typedStrings");
	}
	
	@Test public void testTypedFreshBlanks() throws InvalidFormatException, IOException, ModelIOException {
		runAtomicTest("typedFreshBlanks");
	}
	
	@Test public void testTypedNamedBlanks() throws InvalidFormatException, IOException, ModelIOException {
		runAtomicTest("typedNamedBlanks");
	}
	
	@Test public void testTypedQNameIRI() throws InvalidFormatException, IOException, ModelIOException {
		runAtomicTest("typedQNameIRI");
	}
	
	@Test public void testTypedFullIRI() throws InvalidFormatException, IOException, ModelIOException {
		runAtomicTest("typedFullIRI");
	}
	
	@Test public void testUntypedBooleans() throws InvalidFormatException, IOException, ModelIOException {
		runAtomicTest("untypedBooleans");
	}
	
	@Test public void testUntypedIntergers() throws InvalidFormatException, IOException, ModelIOException {
		runAtomicTest("untypedIntegers");
	}
	
	@Test public void testUntypedDecimals() throws InvalidFormatException, IOException, ModelIOException {
		runAtomicTest("untypedDecimals");
	}
	
	@Test public void testUntypedFreshBlanks() throws InvalidFormatException, IOException, ModelIOException {
		runAtomicTest("untypedFreshBlanks");
	}
	
	@Test public void testUntypedNamedBlanks() throws InvalidFormatException, IOException, ModelIOException {
		runAtomicTest("untypedNamedBlanks");
	}
	
	@Test public void testUntypedQNameIRI() throws InvalidFormatException, IOException, ModelIOException {
		runAtomicTest("untypedQNameIRI");
	}
	
	@Test public void testUntypedFullIRI() throws InvalidFormatException, IOException, ModelIOException {
		runAtomicTest("untypedFullIRI");
	}

	@Test public void testUntypedLiterals() throws InvalidFormatException, IOException, ModelIOException {
		runAtomicTest("untypedLiterals");
	}
	
	@Test public void testTypedUntyped() throws InvalidFormatException, IOException, ModelIOException {
		runAtomicTest("typedText");
	}
}