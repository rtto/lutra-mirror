package osl.util.rdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.AfterClass;
import org.junit.Test;

import xyz.lutra.parser.TemplateLoader;

public class RDFListsTest {
	
	@AfterClass
	public static void clear () throws IOException {
		TemplateLoader.clearCache();
	}
	
	private String folder = "src/test/resources/rdf/";

	private String ex = "http://example.com#";
	private Property hasList = ResourceFactory.createProperty(ex + "hasList");
	
	private RDFList getList (Model model, String localname) {
		return model.listObjectsOfProperty(ResourceFactory.createResource(ex + localname), hasList).next().as(RDFList.class);
	}
	
	@Test public void shouldOutput3Lists1 () {
		Model model = ModelIO.readModel(folder + "lists1.ttl");
		assertEquals(3, RDFLists.getNonEmptyRDFLists(model).size());
	}
	
	private void shouldReplaceLists2 (String oldName, String freshName) {
		Model model = ModelIO.readModel(folder + "lists2.ttl");
		
		// old and fresh are different.
		RDFList old = getList(model, oldName);
		RDFList fresh = getList(model, freshName);
		assertFalse(old.sameListAs(fresh));
		
		RDFLists.substituteNonEmptyRDFList(model, old, fresh);
		
		// old and fresh should now have equal content.
		old = getList(model, oldName);
		fresh = getList(model, freshName);
		assertTrue(old.sameListAs(fresh));
	}
	
	@Test public void shouldReplaceLists2a () {
		shouldReplaceLists2("old1", "fresh1");
	}
	@Test public void shouldReplaceLists2b () {
		shouldReplaceLists2("old3", "fresh3");
	}
	@Test public void shouldReplaceLists2c () {
		shouldReplaceLists2("old4", "fresh4");
	}
	
	@Test public void shouldReplaceLists3 () throws ModelIOException {
		Model model = ModelIO.readModel(folder + "lists2.ttl");
		
		RDFList old1 = getList(model, "old2a");
		RDFList fresh1 = getList(model, "fresh2");
		assertFalse(old1.sameListAs(fresh1));
		
		RDFLists.substituteNonEmptyRDFList(model, old1, fresh1);
		
		old1 = getList(model, "old2b"); // different old, but with the same content as the old2a.
		fresh1 = getList(model, "fresh2");
		assertTrue(old1.sameListAs(fresh1));
		
		//ModelIO.printModel(model, ModelIO.format.TURTLE);
	}
	

}
