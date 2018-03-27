package xyz.lutra;

import org.junit.AfterClass;
import org.junit.Test;

import xyz.lutra.model.Template;
import xyz.lutra.parser.TemplateLoader;

public class stOTTRWriterTest {
	
	public String printTemplate(String iri) {
		Template t = TemplateLoader.getTemplate(iri);
		return t.toString();
	}

	@Test
	public void testSuperObjectExactCardinality() {
		String iri = "http://candidate.ottr.xyz/owl/axiom/SuperObjectExactCardinality";
		System.out.println(printTemplate(iri));
	}
	
	@Test
	public void testNamedPizza() {
		String iri = "http://draft.ottr.xyz/pizza/NamedPizza";
		System.out.println(printTemplate(iri));
	}
	
	@AfterClass
	public static void tearDown() {
		TemplateLoader.clearCache();
	}

}
