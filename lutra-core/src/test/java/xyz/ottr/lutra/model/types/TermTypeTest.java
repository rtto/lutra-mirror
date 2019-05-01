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

import static org.junit.Assert.assertTrue;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.junit.Before;
import org.junit.Test;

import xyz.ottr.lutra.OTTR;

public class TermTypeTest {

    private BasicType byIRI(Resource iri) {
        return TypeFactory.getByIRI(iri);
    }
    
    private BasicType byIRI(String iri) {
        return TypeFactory.getByIRI(iri);
    }

    private BasicType owlOProp;

    @Before
    public void setup() {
        owlOProp = byIRI(OWL.ObjectProperty);
    }

    @Test
    public void shouldBeSubTypes() {
        assertTrue(owlOProp.isSubTypeOf(byIRI(RDFS.Resource)));
        assertTrue(owlOProp.isSubTypeOf(byIRI(OWL.ObjectProperty)));
    }

    @Test
    public void shouldBeCompatible() {
        assertTrue(owlOProp.isCompatibleWith(byIRI(OTTR.Types.IRI)));
        assertTrue(owlOProp.isCompatibleWith(byIRI(RDFS.Resource)));
        assertTrue(owlOProp.isCompatibleWith(byIRI(OWL.ObjectProperty)));
    }

    @Test
    public void shouldBeIncompatible() {
        assertTrue(owlOProp.isIncompatibleWith(byIRI(OWL.DatatypeProperty)));
        assertTrue(owlOProp.isIncompatibleWith(byIRI(OWL.AnnotationProperty)));
        assertTrue(owlOProp.isIncompatibleWith(byIRI(XSD.xstring)));
        assertTrue(owlOProp.isIncompatibleWith(byIRI(RDFS.Literal)));
        assertTrue(owlOProp.isIncompatibleWith(byIRI(XSD.xlong)));
        assertTrue(owlOProp.isIncompatibleWith(byIRI(RDF.HTML)));
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
