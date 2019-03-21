package xyz.ottr.lutra.model.types;

/*-
 * #%L
 * lutra-core
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
import static org.junit.Assert.assertTrue;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.junit.Before;
import org.junit.Test;

public class TermTypeTest {

    private void termTypeIRIByNameTest(String name, Resource iri) throws Exception {
        assertEquals(byName(name).getIRI(), iri.getURI());
    }

    private BasicType byName(String name) throws Exception {
        return TypeFactory.getByName(name);
    }

    private BasicType owlOProp;

    @Before
    public void setup() throws Exception {
        owlOProp = byName("objectproperty");
    }
    
    // @Test public void testRelations() {
    //     for (TermType t1 : BasicType.getAllTermTypes()) {
    //         for (TermType t2 : BasicType.getAllTermTypes()) {
    //             // subtype => moreSpecificThan:
    //             assertTrue(!t1.isSubTypeOf(t2) || t1.isMoreSpecificThan(t2));
    //             
    //             // moreSpecificThan => ! incompatble
    //             assertTrue(!t1.isMoreSpecificThan(t2) || !t1.isIncompatibleWith(t2));
    //         }
    //     }
    // }

    @Test 
    public void shouldGetByName() throws Exception {
        termTypeIRIByNameTest("class", OWL.Class);
        termTypeIRIByNameTest("ReSouRce", RDFS.Resource);
        termTypeIRIByNameTest("int", XSD.xint);
        termTypeIRIByNameTest("integer", XSD.integer);
    }

    @Test
    public void shouldBeSubTypes() throws Exception {
        assertTrue(owlOProp.isSubTypeOf(byName("resource")));
        assertTrue(owlOProp.isSubTypeOf(byName("objectProperty")));
    }

    @Test
    public void shouldBeCompatible() throws Exception {
        assertTrue(owlOProp.isCompatibleWith(byName("IRI")));
        //assertTrue(owlOProp.isCompatibleWith(byName("BlankNode"))); // Sould not be a type
        assertTrue(owlOProp.isCompatibleWith(byName("resource")));
        assertTrue(owlOProp.isCompatibleWith(byName("objectProperty")));
    }

    @Test
    public void shouldBeIncompatible() throws Exception {
        assertTrue(owlOProp.isIncompatibleWith(byName("datatypeproperty")));
        assertTrue(owlOProp.isIncompatibleWith(byName("annotationproperty")));
        assertTrue(owlOProp.isIncompatibleWith(byName("string")));
        assertTrue(owlOProp.isIncompatibleWith(byName("Literal")));
        assertTrue(owlOProp.isIncompatibleWith(byName("long")));
        assertTrue(owlOProp.isIncompatibleWith(byName("HTML")));
    }

    /* For debugging
    @Test
    public void test0() throws ModelIOException {
        ModelIO.printModel(TermType.getModel(), ModelIO.Format.TURTLE);
    }

    @Test
    public void test1() throws Exception {
        Resource op = ResourceFactory.createResource(owlOProp.getIRI());

        for (Property p : new Property[]{OWL.disjointWith, RDFS.subClassOf, OTTR.unifiableWith}) {

            System.out.println(p.getLocalName());
            for (Statement t : TermType.getModel().listStatements(op, p, (RDFNode)null).toList()) {
                System.out.println("\t"
                        + t.getSubject().getLocalName()
                        + " -- "
                        + t.getObject().asResource().getLocalName());
            }
        }
    }*/

}
