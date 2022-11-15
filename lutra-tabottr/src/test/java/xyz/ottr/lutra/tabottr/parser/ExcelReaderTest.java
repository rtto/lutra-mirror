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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.Test;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.system.ResultConsumer;
import xyz.ottr.lutra.system.ResultStream;
import xyz.ottr.lutra.wottr.io.RDFIO;
import xyz.ottr.lutra.wottr.parser.WInstanceParser;
import xyz.ottr.lutra.wottr.writer.WInstanceWriter;

public class ExcelReaderTest {

    private static final Path ROOT = Paths.get("src", "test", "resources");

    private Model writeToModel(ResultStream<Instance> instances) {
        WInstanceWriter writer = new WInstanceWriter();
        ResultConsumer<Instance> consumer = new ResultConsumer<>(writer);
        instances.forEach(consumer);
        Model model = writer.writeToModel();
        return model;
    }
    
    private void runAtomicTest(String name) {
        Path folder = ROOT.resolve("atomic");
        String excelFile = folder.resolve(name + ".xlsx").toString();
        String rdfFile = folder.resolve(name + ".ttl").toString();

        Model excelModel = writeToModel(new ExcelReader().apply(excelFile));
        excelModel.setNsPrefix("ex", "http://example.org#");

        Model rdfInput = RDFIO.fileReader().parse(rdfFile).get();
        assertNotNull(rdfInput);

        Model rdfModel = writeToModel(new WInstanceParser().apply(rdfInput));

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
    
    @Test
    public void testTypedBooleans() {
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
    
    @Test public void testTypedText() {
        runAtomicTest("typedText");
    }

    @Test public void testTypedList() {
        runAtomicTest("typedList");
    }
}
