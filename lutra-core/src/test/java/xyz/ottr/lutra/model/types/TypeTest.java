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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.ListTerm;
import xyz.ottr.lutra.model.terms.LiteralTerm;

public class TypeTest {

    private BasicType byIRI(Resource iri) {
        return TypeRegistry.asType(iri);
    }
    
    private BasicType byIRI(String iri) {
        return TypeRegistry.asType(iri);
    }

    private BasicType owlOProp;

    @BeforeEach
    public void setup() {

        this.owlOProp = byIRI(OWL.ObjectProperty);
    }

    @Test
    public void shouldBeSubTypes() {
        assertTrue(this.owlOProp.isSubTypeOf(byIRI(RDFS.Resource)));
        assertTrue(this.owlOProp.isSubTypeOf(byIRI(OWL.ObjectProperty)));
    }

    @Test
    public void shouldBeCompatible() {
        assertTrue(this.owlOProp.isCompatibleWith(byIRI(OTTR.TypeURI.IRI)));
        assertTrue(this.owlOProp.isCompatibleWith(byIRI(RDFS.Resource)));
        assertTrue(this.owlOProp.isCompatibleWith(byIRI(OWL.ObjectProperty)));
    }

    @Test
    public void shouldBeIncompatible() {
        assertTrue(this.owlOProp.isIncompatibleWith(byIRI(OWL.DatatypeProperty)));
        assertTrue(this.owlOProp.isIncompatibleWith(byIRI(OWL.AnnotationProperty)));
        assertTrue(this.owlOProp.isIncompatibleWith(byIRI(XSD.xstring)));
        assertTrue(this.owlOProp.isIncompatibleWith(byIRI(RDFS.Literal)));
        assertTrue(this.owlOProp.isIncompatibleWith(byIRI(XSD.xlong)));
        assertTrue(this.owlOProp.isIncompatibleWith(byIRI(RDF.HTML)));
    }
    
    @Test
    public void listCompatibility() {
        final ListType listType = TypeRegistry.LIST_TYPE;
        final NEListType neListType = TypeRegistry.NELIST_TYPE;

        // compatible with self
        assertTrue(listType.isCompatibleWith(listType));
        assertTrue(neListType.isCompatibleWith(neListType));

        // nelist compatible with list, but not reverse
        assertTrue(neListType.isCompatibleWith(listType));
        assertTrue(listType.isIncompatibleWith(neListType));
        assertTrue(new NEListType(this.owlOProp).isCompatibleWith(new ListType(this.owlOProp)));
        assertTrue(new ListType(this.owlOProp).isIncompatibleWith(new NEListType(this.owlOProp)));

        // empty list type is listTerm and not nelist
        var emptyListType = new ListTerm(Collections.EMPTY_LIST).getType();

        assertEquals(emptyListType, listType);
        assertNotEquals(emptyListType, neListType);
        assertTrue(emptyListType.isCompatibleWith(listType));
        assertTrue(emptyListType.isIncompatibleWith(neListType));

        // non-empty list type is nelistTerm and not list
        var nonEmptyListType = new ListTerm(new IRITerm("http://example.com")).getType();

        assertNotEquals(nonEmptyListType, emptyListType);

        // NB! this can break should we decide to make list types more specific, e.g., NEList<ottr:IRI>
        assertEquals(nonEmptyListType, neListType);

        assertTrue(nonEmptyListType.isCompatibleWith(listType));
        assertTrue(nonEmptyListType.isCompatibleWith(neListType));
    }

    /* For debugging
    @Test
    public void test0() throws ModelIOException {
        ModelIO.printModel(Type.getModel(), ModelIO.Format.TURTLE);
    }

    @Test
    public void test1() throws Exception {
        Resource op = ResourceFactory.createResource(owlOProp.getIri());

        for (Property p : new Property[]{OWL.disjointWith, RDFS.subClassOf, OTTR.unifiableWith}) {

            System.out.println(p.getLocalName());
            for (Statement t : Type.getModel().listStatements(op, p, (RDFNode)null).toList()) {
                System.out.println("\t"
                        + t.getSubject().getLocalName()
                        + " -- "
                        + t.getObject().asResource().getLocalName());
            }
        }
    }*/

}
