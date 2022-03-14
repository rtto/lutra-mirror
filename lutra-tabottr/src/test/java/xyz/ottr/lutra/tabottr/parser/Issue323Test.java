package xyz.ottr.lutra.tabottr.parser;

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
import static org.junit.Assert.assertSame;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.Test;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.tabottr.TabOTTR;

public class Issue323Test {

    private static final String LEGAL_NS = "http://example.net#";
    private static final String BLANK_NS = "http://example.net #";

    @Test
    public void legalIRI() {
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("ex", LEGAL_NS);
        RDFNodeFactory factory = new RDFNodeFactory(model);

        Resource ann = ResourceFactory.createResource("http://example.net#Ann");
        
        assertEquals(ann, factory.toRDFNode("ex:Ann", TabOTTR.TYPE_AUTO).get());
        assertEquals(ann, factory.toRDFNode("ex:Ann", TabOTTR.TYPE_IRI).get());
        assertEquals(ann, factory.toRDFNode("http://example.net#Ann", TabOTTR.TYPE_AUTO).get());
        assertEquals(ann, factory.toRDFNode("http://example.net#Ann", TabOTTR.TYPE_IRI).get());

        model.close();
    }

    @Test
    public void namespaceWithBlank() {
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("ex", BLANK_NS);
        RDFNodeFactory factory = new RDFNodeFactory(model);

        Result<RDFNode> iriResult = factory.toRDFNode("ex:Ann", TabOTTR.TYPE_IRI);
        assertSame(Message.Severity.ERROR, iriResult.getMessageHandler().getMostSevere());

        model.close();
    }

    @Test
    public void valuePartWithBlank() {
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("ex", LEGAL_NS);
        RDFNodeFactory factory = new RDFNodeFactory(model);

        Result<RDFNode> iriResult = factory.toRDFNode("ex: Ann", TabOTTR.TYPE_IRI);
        assertSame(Message.Severity.ERROR, iriResult.getMessageHandler().getMostSevere());

        model.close();
    }

}
