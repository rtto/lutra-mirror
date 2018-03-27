package osl.util.rdf;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.OWL;
import org.junit.AfterClass;
import org.junit.Test;

import xyz.lutra.parser.TemplateLoader;

public class ModelIOTest {

	private String resources = "src/test/resources/rdf/";
	
	@AfterClass
	public static void clear () throws IOException {
		TemplateLoader.clearCache();
	}
	
	@Test
	public void shouldReadModel () {
		Model m = ModelIO.readModel(resources + "model.ttl");
		assertEquals(m.size(), 2);
	}
	
	@Test
	public void shouldPrintModel () throws ModelIOException {
		Model m = ModelIO.readModel(resources + "model.ttl");
		ModelIO.printModel(m, ModelIO.format.N3);
		ModelIO.printModel(m, ModelIO.format.NTRIPLES);
		ModelIO.printModel(m, ModelIO.format.RDFXML);
		ModelIO.printModel(m, ModelIO.format.TURTLE);
	}
	
	@Test
	public void shouldShortForm () {
		Model m = ModelIO.readModel(resources + "model.ttl");
		assertEquals(ModelIO.shortForm(m, OWL.Class), "owl:Class");
	}
}
