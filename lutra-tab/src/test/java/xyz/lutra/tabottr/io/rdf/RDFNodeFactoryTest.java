package xyz.lutra.tabottr.io.rdf;

import static org.junit.Assert.*;

import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.XSD;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import osl.util.rdf.Models;
import osl.util.rdf.vocab.Templates;
import xyz.lutra.tabottr.TabOTTR;
import xyz.lutra.tabottr.io.rdf.RDFNodeFactory;

public class RDFNodeFactoryTest {

	public final static String NS = "http://example.net#";
	
	private Model model;
	private RDFNodeFactory factory;
	
	@Before
	public void setUp() throws Exception {
		model = Models.empty();
		model.setNsPrefix("ex", "http://example.net#");
		factory = new RDFNodeFactory(model);
	}

	@After
	public void tearDown() throws Exception {
		model.close();
	}

	@Test
	public void shouldGetNoValue() {
		assertEquals(Templates.none, factory.toRDFNode("", TabOTTR.TYPE_AUTO));
		assertEquals(Templates.none, factory.toRDFNode("", TabOTTR.TYPE_IRI));
		assertEquals(Templates.none, factory.toRDFNode("", TabOTTR.TYPE_BLANK));
		assertEquals(Templates.none, factory.toRDFNode("", TabOTTR.TYPE_TEXT));
	}
	
	@Test
	public void shouldGetIRI() {
		Resource ann = ResourceFactory.createResource(NS + "Ann");
		
		assertEquals(ann, factory.toRDFNode("ex:Ann", TabOTTR.TYPE_AUTO));
		assertEquals(ann, factory.toRDFNode("ex:Ann", TabOTTR.TYPE_IRI));
		assertEquals(ann, factory.toRDFNode("http://example.net#Ann", TabOTTR.TYPE_AUTO));
		assertEquals(ann, factory.toRDFNode("http://example.net#Ann", TabOTTR.TYPE_IRI));
	}
	
	@Test
	public void shouldGetNamedBlank() {
		Resource myBlank = model.createResource(AnonId.create("myBlank"));
		
		assertEquals(myBlank, factory.toRDFNode("_:myBlank", TabOTTR.TYPE_AUTO));
		assertEquals(myBlank, factory.toRDFNode("_:myBlank", TabOTTR.TYPE_BLANK));
		assertEquals(myBlank, factory.toRDFNode("myBlank", TabOTTR.TYPE_BLANK));
	}
	
	@Test
	public void shouldGetFreshBlank() {
		// untyped:
		RDFNode fresh1 = factory.toRDFNode(TabOTTR.VALUE_FRESH_BLANK, TabOTTR.TYPE_AUTO);
		RDFNode fresh2 = factory.toRDFNode(TabOTTR.VALUE_FRESH_BLANK, TabOTTR.TYPE_AUTO);
		
		assertTrue(fresh1.isAnon());
		assertTrue(fresh2.isAnon());
		assertNotEquals(fresh1, fresh2); // two freshs must no be equal
		
		// typed:
		RDFNode fresh3 = factory.toRDFNode(TabOTTR.VALUE_FRESH_BLANK, TabOTTR.TYPE_BLANK);
		RDFNode fresh4 = factory.toRDFNode(TabOTTR.VALUE_FRESH_BLANK, TabOTTR.TYPE_BLANK);
		
		assertTrue(fresh3.isAnon());
		assertTrue(fresh4.isAnon());
		assertNotEquals(fresh3, fresh4);
	}
	
	@Test
	public void shouldGetTypedLiteralsInteger() {
		Literal literal = model.createTypedLiteral(3, XSD.integer.toString());
		
		assertEquals(literal, factory.toRDFNode("3", TabOTTR.TYPE_AUTO));
		assertEquals(literal, factory.toRDFNode("3", XSD.integer.toString()));
	}
	
	@Test
	public void shouldGetTypedLiteralsDecimal() {
		Literal literal = model.createTypedLiteral("3.14", XSD.decimal.toString());
		
		assertEquals(literal, factory.toRDFNode("3.14", TabOTTR.TYPE_AUTO));
		assertEquals(literal, factory.toRDFNode("3.14", XSD.decimal.toString()));
	}
	
	@Test
	public void shouldGetTypedLiteralsBoolean() {
		Literal literal = model.createTypedLiteral(true);
		
		assertEquals(literal, factory.toRDFNode("true", TabOTTR.TYPE_AUTO));
		assertEquals(literal, factory.toRDFNode("1", XSD.xboolean.toString()));
		assertEquals(literal, factory.toRDFNode("true", XSD.xboolean.toString()));
		assertEquals(literal, factory.toRDFNode("True", XSD.xboolean.toString()));
		assertEquals(literal, factory.toRDFNode("TRUE", XSD.xboolean.toString()));
	}
	
	@Test
	public void shouldGetTypedLiteralsString() {
		Literal literal = model.createTypedLiteral("hello");
		
		assertEquals(literal, factory.toRDFNode("hello", XSD.xstring.toString()));
	}
	
	@Test
	public void shouldGetUntypedLiteral() {
		Literal literal = model.createLiteral("hello");
		
		assertEquals(literal, factory.toRDFNode("hello", TabOTTR.TYPE_AUTO));
		assertEquals(literal, factory.toRDFNode("hello", TabOTTR.TYPE_TEXT));
	}
	
	@Test
	public void shouldGetUntypedLiteralWLang() {
		Literal literal = model.createLiteral("hello", "en-GB");
		
		assertEquals(literal, factory.toRDFNode("hello@@en-GB", TabOTTR.TYPE_AUTO));
		assertEquals(literal, factory.toRDFNode("hello@@en-GB", TabOTTR.TYPE_TEXT));
	}
	
	@Test(expected = IllegalArgumentException.class) 
	public void shouldThrowEmptyType() {
		factory.toRDFNode("hello@@en-GB", "");
	}
	
	@Test(expected = IllegalArgumentException.class) 
	public void shouldThrowNonIRIType() {
		factory.toRDFNode("hello@@en-GB", "1234567890");
	}
	
}
