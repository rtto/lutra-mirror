package xyz.ottr.lutra.tabottr.io.rdf;

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

import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.XSD;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import xyz.ottr.lutra.tabottr.TabOTTR;
import xyz.ottr.lutra.tabottr.io.rdf.RDFNodeFactory;
import xyz.ottr.lutra.wottr.WOTTR;

public class RDFNodeFactoryTest {

    public static final String NS = "http://example.net#";
    
    private Model model;
    private RDFNodeFactory factory;
    
    @Before
    public void setUp() throws Exception {
        model = ModelFactory.createDefaultModel();
        model.setNsPrefix("ex", "http://example.net#");
        factory = new RDFNodeFactory(model);
    }

    @After
    public void tearDown() throws Exception {
        model.close();
    }

    @Test
    public void shouldGetNoValue() {
        assertEquals(WOTTR.none, factory.toRDFNode("", TabOTTR.TYPE_AUTO).get());
        assertEquals(WOTTR.none, factory.toRDFNode("", TabOTTR.TYPE_IRI).get());
        assertEquals(WOTTR.none, factory.toRDFNode("", TabOTTR.TYPE_BLANK).get());
        assertEquals(WOTTR.none, factory.toRDFNode("", TabOTTR.TYPE_TEXT).get());
    }
    
    @Test
    public void shouldGetIRI() {
        Resource ann = ResourceFactory.createResource(NS + "Ann");
        
        assertEquals(ann, factory.toRDFNode("ex:Ann", TabOTTR.TYPE_AUTO).get());
        assertEquals(ann, factory.toRDFNode("ex:Ann", TabOTTR.TYPE_IRI).get());
        assertEquals(ann, factory.toRDFNode("http://example.net#Ann", TabOTTR.TYPE_AUTO).get());
        assertEquals(ann, factory.toRDFNode("http://example.net#Ann", TabOTTR.TYPE_IRI).get());
    }
    
    @Test
    public void shouldGetNamedBlank() {
        Resource myBlank = model.createResource(AnonId.create("myBlank"));
        
        assertEquals(myBlank, factory.toRDFNode("_:myBlank", TabOTTR.TYPE_AUTO).get());
        assertEquals(myBlank, factory.toRDFNode("_:myBlank", TabOTTR.TYPE_BLANK).get());
        assertEquals(myBlank, factory.toRDFNode("myBlank", TabOTTR.TYPE_BLANK).get());
    }
    
    @Test
    public void shouldGetFreshBlank() {
        // untyped:
        RDFNode fresh1 = factory.toRDFNode(TabOTTR.VALUE_FRESH_BLANK, TabOTTR.TYPE_AUTO).get();
        RDFNode fresh2 = factory.toRDFNode(TabOTTR.VALUE_FRESH_BLANK, TabOTTR.TYPE_AUTO).get();
        
        assertTrue(fresh1.isAnon());
        assertTrue(fresh2.isAnon());
        //assertNotEquals(fresh1, fresh2); // two freshs must no be equal
        assertTrue(!fresh1.equals(fresh2)); // The above is not found by compiler
        
        // typed:
        RDFNode fresh3 = factory.toRDFNode(TabOTTR.VALUE_FRESH_BLANK, TabOTTR.TYPE_BLANK).get();
        RDFNode fresh4 = factory.toRDFNode(TabOTTR.VALUE_FRESH_BLANK, TabOTTR.TYPE_BLANK).get();
        
        assertTrue(fresh3.isAnon());
        assertTrue(fresh4.isAnon());
        //assertNotEquals(fresh3, fresh4);
        assertTrue(!fresh3.equals(fresh4)); // The above is not found by compiler
    }
    
    @Test
    public void shouldGetTypedLiteralsInteger() {
        Literal literal = model.createTypedLiteral(3, XSD.integer.toString());
        
        assertEquals(literal, factory.toRDFNode("3", TabOTTR.TYPE_AUTO).get());
        assertEquals(literal, factory.toRDFNode("3", XSD.integer.toString()).get());
    }
    
    @Test
    public void shouldGetTypedLiteralsDecimal() {
        Literal literal = model.createTypedLiteral("3.14", XSD.decimal.toString());
        
        assertEquals(literal, factory.toRDFNode("3.14", TabOTTR.TYPE_AUTO).get());
        assertEquals(literal, factory.toRDFNode("3.14", XSD.decimal.toString()).get());
    }
    
    @Test
    public void shouldGetTypedLiteralsBoolean() {
        Literal literal = model.createTypedLiteral(true);
        
        assertEquals(literal, factory.toRDFNode("true", TabOTTR.TYPE_AUTO).get());
        assertEquals(literal, factory.toRDFNode("1", XSD.xboolean.toString()).get());
        assertEquals(literal, factory.toRDFNode("true", XSD.xboolean.toString()).get());
        assertEquals(literal, factory.toRDFNode("True", XSD.xboolean.toString()).get());
        assertEquals(literal, factory.toRDFNode("TRUE", XSD.xboolean.toString()).get());
    }
    
    @Test
    public void shouldGetTypedLiteralsString() {
        Literal literal = model.createTypedLiteral("hello");
        
        assertEquals(literal, factory.toRDFNode("hello", XSD.xstring.toString()).get());
    }
    
    @Test
    public void shouldGetUntypedLiteral() {
        Literal literal = model.createLiteral("hello");
        
        assertEquals(literal, factory.toRDFNode("hello", TabOTTR.TYPE_AUTO).get());
        assertEquals(literal, factory.toRDFNode("hello", TabOTTR.TYPE_TEXT).get());
    }
    
    @Test
    public void shouldGetUntypedLiteralWLang() {
        Literal literal = model.createLiteral("hello", "en-GB");
        
        assertEquals(literal, factory.toRDFNode("hello@@en-GB", TabOTTR.TYPE_AUTO).get());
        assertEquals(literal, factory.toRDFNode("hello@@en-GB", TabOTTR.TYPE_TEXT).get());
    }
    
    @Test
    public void shouldThrowEmptyType() {
        assertFalse(factory.toRDFNode("hello@@en-GB", "").isPresent());
    }
    
    @Test
    public void shouldThrowNonIRIType() {
        assertFalse(factory.toRDFNode("hello@@en-GB", "1234567890").isPresent());
    }
}
