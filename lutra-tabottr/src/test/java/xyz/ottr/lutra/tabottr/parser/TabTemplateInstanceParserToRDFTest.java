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

import static org.junit.Assert.assertTrue;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.PrefixMapping;
import org.junit.Test;

import xyz.ottr.lutra.io.InstanceParser;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.result.ResultConsumer;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.tabottr.parser.ExcelReader;
import xyz.ottr.lutra.wottr.legacy.io.WInstanceWriter;
import xyz.ottr.lutra.wottr.legacy.io.WReader;
import xyz.ottr.lutra.wottr.util.ModelIO;

public class TabTemplateInstanceParserToRDFTest {
    
    private static final String ROOT = "src/test/resources/";
    
    private Model getExcelReaderRDFWriterModel(String filename) {
        InstanceParser<String> parser = new ExcelReader();
        ResultStream<Instance> instances = parser.apply(filename);
        WInstanceWriter writer = new WInstanceWriter();
        ResultConsumer<Instance> consumer = new ResultConsumer<>(writer);
        instances.forEach(consumer);
        Model model = WReader.getCanonicalModel(writer.writeToModel()); 
        return model;
    }
    
    private void runAtomicTest(String name) {
        String folder = ROOT + "atomic/";
        String excelFile = folder + name + ".xlsx";
        String rdfFile = folder + name + ".ttl";
        Model excelModel = getExcelReaderRDFWriterModel(excelFile);
        excelModel.setNsPrefix("ex", "http://example.org#");
        Model rdfModel = WReader.getCanonicalModel(ModelIO.readModel(rdfFile));
        rdfModel.setNsPrefixes(PrefixMapping.Standard);

        boolean isIsomorphic = excelModel.isIsomorphicWith(rdfModel);
        /*
        // For debugging:
        if (!isIsomorphic) {
            System.out.println("Error: excelFile " + excelFile + " not isomorphic to rdfFile " + rdfFile);
            System.out.println("excelFile:");
            excelModel.write(System.out, "TTL");
            System.out.println("rdfFile:");
            rdfModel.write(System.out, "TTL");
        }*/
        assertTrue(isIsomorphic);
    }
    
    @Test public void testTypedBooleans() {
        runAtomicTest("typedBooleans");
    }
    
    @Test public void testTypedInts() {
        runAtomicTest("typedInts");
    }
    
    @Test public void testTypedIntergers() {
        runAtomicTest("typedIntegers");
    }
    
    @Test public void testTypedDecimals() {
        runAtomicTest("typedDecimals");
    }
    
    @Test public void testTypedStrings() {
        runAtomicTest("typedStrings");
    }
    
    @Test public void testTypedFreshBlanks() {
        runAtomicTest("typedFreshBlanks");
    }
    
    @Test public void testTypedNamedBlanks() {
        runAtomicTest("typedNamedBlanks");
    }
    
    @Test public void testTypedQNameIRI() {
        runAtomicTest("typedQNameIRI");
    }
    
    @Test public void testTypedFullIRI() {
        runAtomicTest("typedFullIRI");
    }
    
    @Test public void testUntypedBooleans() {
        runAtomicTest("untypedBooleans");
    }
    
    @Test public void testUntypedIntergers() {
        runAtomicTest("untypedIntegers");
    }
    
    @Test public void testUntypedDecimals() {
        runAtomicTest("untypedDecimals");
    }
    
    @Test public void testUntypedFreshBlanks() {
        runAtomicTest("untypedFreshBlanks");
    }
    
    @Test public void testUntypedNamedBlanks() {
        runAtomicTest("untypedNamedBlanks");
    }
    
    @Test public void testUntypedQNameIRI() {
        runAtomicTest("untypedQNameIRI");
    }
    
    @Test public void testUntypedFullIRI() {
        runAtomicTest("untypedFullIRI");
    }

    @Test public void testUntypedLiterals() {
        runAtomicTest("untypedLiterals");
    }
    
    @Test public void testTypedUntyped() {
        runAtomicTest("typedText");
    }
}
