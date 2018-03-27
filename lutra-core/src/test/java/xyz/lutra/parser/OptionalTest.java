package xyz.lutra.parser;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import osl.util.rdf.ModelIO;
import osl.util.rdf.ModelIOException;
import osl.util.rdf.Models;
import xyz.lutra.Expander;
import xyz.lutra.parser.ParserException;
import xyz.lutra.parser.TemplateLoader;

public class OptionalTest {

	private static final String ROOT = "src/test/resources/test/optional/";
	private static final String EX = "http://example.com#";

	private static final String[] library = {
			ROOT + "Triple.ttl",
			ROOT + "TestTemplate.ttl",
			ROOT + "Test3Template.ttl",
            ROOT + "optional-triple.ttl"
			};
	
	@Before
	public void load () throws IOException {
		TemplateLoader.load(library);
	}

	@After
	public void clear () throws IOException {
		TemplateLoader.clearCache();
	}
	
	private Resource ex (String localname) {
		return ResourceFactory.createResource(EX + localname);
	}

	@Test
	public void optionalTest3 () throws ParserException, IOException, ModelIOException {
		Model exp1 = Expander.expand(ROOT + "optional-triple-instances.ttl");
        Model exp2 = ModelIO.readModel(ROOT + "optional-triple-expanded.ttl");
        assertTrue(exp1.isIsomorphicWith(exp2));
	}
	
	@Test
	public void optionalTest1 () throws ParserException, IOException, ModelIOException {
		Model m = Expander.expandTemplate(ROOT + "test1.ttl");
		
		Model sol = Models.empty();
		sol.add(ex("A"), RDF.type, ex("Type1"));
		sol.add(ex("B"), RDF.type, ex("Type2"));
		sol.add(ex("C"), RDF.type, ex("Type3"));
		assertTrue(m.isIsomorphicWith(sol));
	}
	
	@Test
	public void optionalTest2 () throws ParserException, IOException, ModelIOException {
		Model m = Expander.expandTemplate(ROOT + "test2.ttl");

		Model sol = Models.empty();
		sol.add(ex("A"), RDF.type, ex("Type1"));
		sol.add(ex("C"), RDF.type, ex("Type3"));
		assertTrue(m.isIsomorphicWith(sol));
	}
}
