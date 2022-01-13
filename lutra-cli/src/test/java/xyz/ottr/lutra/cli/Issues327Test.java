package xyz.ottr.lutra.cli;

/*-
 * #%L
 * xyz.ottr.lutra:lutra-cli
 * %%
 * Copyright (C) 2018 - 2021 University of Oslo
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

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;

public class Issues327Test {
    
    private static final String ROOT = "src/test/resources/issues/327/";

    private static final String input = ROOT + "instances1.stottr";
    private static final String expectedOutputTTL = ROOT + "expected_output1.ttl";
    
    /*
     * BUG Incomplete expansion: https://gitlab.com/ottr/lutra/lutra/-/issues/327
     */


    // Test writing expansion to console.
    @Test
    public void testOutTTLConsole() {
        // Safe-keep System.out
        final PrintStream stdOut = System.out;

        // Create stream to capture System.out:
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8);
        System.setOut(ps);

        CLIRunner.run("--mode expand --inputFormat stottr --fetchMissing " + input);

        // Restore old out
        stdOut.flush();
        System.setOut(stdOut);

        // parse captured console output to model
        Model actual = ModelFactory.createDefaultModel();
        RDFDataMgr.read(actual, new StringReader(baos.toString(StandardCharsets.UTF_8)), "", Lang.TURTLE);

        Model expected = RDFDataMgr.loadModel(expectedOutputTTL);

        assertThat("should contain the same number of triples", actual.size(), is(expected.size()));
        assertTrue(expected.isIsomorphicWith(actual));
    }

    // Test writing expansion to turtle file.
    @Test
    public void testOutTTLFile() {

        String outFile = "testOutTTLFile.ttl";

        CLIRunner.run("--mode expand --inputFormat stottr --fetchMissing " + input + " -o " + ROOT + outFile);

        Model actual = RDFDataMgr.loadModel(ROOT + outFile);
        Model expected = RDFDataMgr.loadModel(expectedOutputTTL);

        assertThat("should contain the same number of triples", actual.size(), is(expected.size()));
        assertTrue(expected.isIsomorphicWith(actual));
    }
}
