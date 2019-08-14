package xyz.ottr.lutra.wottr.parser.v04;

/*-
 * #%L
 * lutra-wottr
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
import static org.junit.Assert.fail;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import xyz.ottr.lutra.model.TemplateSignature;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.wottr.vocabulary.v04.WOTTR;

public class RDFTemplateParserTest {

    private static WTemplateParser templateParser;

    @BeforeClass    
    public static void load() {
        templateParser = new WTemplateParser();
    }

    @Test
    public void shouldNotParse() {

        Model model = ModelFactory.createDefaultModel();
        ResultStream<TemplateSignature> empty = templateParser.apply(model);
        empty.innerForEach(none -> fail());

        Resource templateIRI = model.createResource("http://example.org/template");
        model.add(model.createStatement(templateIRI, RDF.type, WOTTR.Template));

        ResultStream<TemplateSignature> onlyURI = templateParser.apply(model);
        onlyURI.innerForEach(none -> fail());

        RDFList paramLst = model.createList();
        Resource value = model.createResource("http://example.org#param");
        paramLst = paramLst.cons(value);
        model.add(model.createStatement(templateIRI, WOTTR.parameters, paramLst));

        ResultStream<TemplateSignature> noBody = templateParser.apply(model);
        noBody.innerForEach(none -> fail());
    }

    @AfterClass
    public static void clear() {
        templateParser = null;
    }
}
