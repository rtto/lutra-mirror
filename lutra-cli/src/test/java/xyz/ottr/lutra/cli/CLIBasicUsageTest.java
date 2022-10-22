package xyz.ottr.lutra.cli;

/*-
 * #%L
 * xyz.ottr.lutra:lutra-cli
 * %%
 * Copyright (C) 2018 - 2022 University of Oslo
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Ignore;
import org.junit.Test;


public class CLIBasicUsageTest {
    private static final String ROOT = "src/test/resources/CLIBasicUsage/";

    /** -m  expand **/
    // -I stottr
    @Test
    public void expand_stottr_instances() {
        String args = " "
                + " -l " + ROOT + "person_template.stottr"
                + " -L stottr "
                + ROOT + "person_instances.stottr"
                + " -I stottr";

        // Safe-keep System.out
        final PrintStream stdOut = System.out;

        // Create stream to capture System.out:
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8);
        System.setOut(ps);

        //c.executeArgs(args.trim().split("\\s+"));
        CLIRunner.run(args);

        // Restore old out
        stdOut.flush();
        System.setOut(stdOut);

        // parse captured console output to model
        Model actual = ModelFactory.createDefaultModel();
        RDFDataMgr.read(actual, new StringReader(baos.toString(StandardCharsets.UTF_8)), "", Lang.TURTLE);

        Model expected = RDFDataMgr.loadModel(ROOT + "expected_expand_stottr.ttl");

        assertThat("should contain the same number of triples", actual.size(), is(expected.size()));
        TestUtils.testIsomorphicModels(actual, expected);
    }

    // -I wottr
    @Test
    public void expand_wottr_instances() {
        String args = " "
                + " -l " + ROOT + "person_template.stottr"
                + " -L stottr "
                + ROOT + "person_instances.wottr"
                + " -I wottr";

        // Safe-keep System.out
        final PrintStream stdOut = System.out;

        // Create stream to capture System.out:
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8);
        System.setOut(ps);

        CLIRunner.run(args);

        // Restore old out
        stdOut.flush();
        System.setOut(stdOut);

        // parse captured console output to model
        Model actual = ModelFactory.createDefaultModel();
        RDFDataMgr.read(actual, new StringReader(baos.toString(StandardCharsets.UTF_8)), "", Lang.TURTLE);

        Model expected = RDFDataMgr.loadModel(ROOT + "expected_expand_stottr.ttl");

        assertThat("should contain the same number of triples", actual.size(), is(expected.size()));
        TestUtils.testIsomorphicModels(actual, expected);

    }

    // -I tabottr
    @Test
    public void expand_tabottr_instances() {
        String args = "-I tabottr -f --stdout " + ROOT + "NamedPizza-instances.xlsx";

        // Safe-keep System.out
        final PrintStream stdOut = System.out;

        // Create stream to capture System.out:
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8);
        System.setOut(ps);
        CLIRunner.run(args);

        // Restore old out
        stdOut.flush();
        System.setOut(stdOut);

        // parse captured console output to model
        Model actual = ModelFactory.createDefaultModel();
        RDFDataMgr.read(actual, new StringReader(baos.toString(StandardCharsets.UTF_8)), "", Lang.TURTLE);

        //TODO: make expected output file
        //Model expected = RDFDataMgr.loadModel(ROOT + "");

        //assertThat("should contain the same number of triples", actual.size(), is(expected.size()));
        //TestUtils.testIsomorphicModels(actual, expected);
    }

    @Test
    public void expand_tabottr_instances2() {
        String args = "-I tabottr -f --stdout " + ROOT + "PizzaOntology-instances.xlsx";

        // Safe-keep System.out
        final PrintStream stdOut = System.out;

        // Create stream to capture System.out:
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8);
        System.setOut(ps);
        CLIRunner.run(args);

        // Restore old out
        stdOut.flush();
        System.setOut(stdOut);

        // parse captured console output to model
        Model actual = ModelFactory.createDefaultModel();
        RDFDataMgr.read(actual, new StringReader(baos.toString(StandardCharsets.UTF_8)), "", Lang.TURTLE);

        //TODO: make expected output file
        //Model expected = RDFDataMgr.loadModel(ROOT + "");

        //assertThat("should contain the same number of triples", actual.size(), is(expected.size()));
        //TestUtils.testIsomorphicModels(actual, expected);
    }

    // -I bottr
    @Test
    public void expand_bottr_instances() {
        String bottrRoot = "../lutra-bottr/src/test/resources/maps/";

        String args = "-I bottr -f --stdout -p "
                + bottrRoot + "instanceMapRDFSource.ttl "
                + bottrRoot + "instanceMapRDFSource.ttl";

        // Safe-keep System.out
        final PrintStream stdOut = System.out;

        // Create stream to capture System.out:
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8);
        System.setOut(ps);

        CLIRunner.run(args);

        // Restore old out
        stdOut.flush();
        System.setOut(stdOut);

        // parse captured console output to model
        Model actual = ModelFactory.createDefaultModel();
        RDFDataMgr.read(actual, new StringReader(baos.toString(StandardCharsets.UTF_8)), "", Lang.TURTLE);

        Model expected = RDFDataMgr.loadModel(ROOT + "expected_expand_bottr.ttl");

        assertThat("should contain the same number of triples", actual.size(), is(expected.size()));
        TestUtils.testIsomorphicModels(actual, expected);
    }

    // --fetchMissing
    @Test
    public void fetchMissing_and_expand_to_console() {
        // Safe-keep System.out
        final PrintStream stdOut = System.out;

        // Create stream to capture System.out:
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8);
        System.setOut(ps);

        CLIRunner.run("--mode expand --inputFormat stottr --fetchMissing " + ROOT + "pizza_instances.stottr");

        // Restore old out
        stdOut.flush();
        System.setOut(stdOut);

        // parse captured console output to model
        Model actual = ModelFactory.createDefaultModel();
        RDFDataMgr.read(actual, new StringReader(baos.toString(StandardCharsets.UTF_8)), "", Lang.TURTLE);

        Model expected = RDFDataMgr.loadModel(ROOT + "expected_output_pizza.ttl");

        assertThat("should contain the same number of triples", actual.size(), is(expected.size()));
        TestUtils.testIsomorphicModels(actual, expected);
    }

    // --output
    @Test
    public void fetchMissing_and_expand_to_file() {

        String outFile = ROOT + "pizza_out_file.ttl";

        CLIRunner.run("--mode expand --inputFormat stottr --fetchMissing "
                + ROOT + "pizza_instances.stottr" + " --output " + outFile);

        assertTrue(new File(outFile).exists());

        Model actual = RDFDataMgr.loadModel(outFile);
        Model expected = RDFDataMgr.loadModel(ROOT + "expected_output_pizza.ttl");

        assertThat("should contain the same number of triples", actual.size(), is(expected.size()));
        TestUtils.testIsomorphicModels(actual, expected);
    }

    /** -m expandLibrary **/
    @Test
    public void expandLibrary() {

    }

    /** -m format **/
    @Test
    public void format() {

    }

    /** -m formatLibrary **/
    @Test
    public void formatLibrary() {

    }

    /** -m lint **/
    @Test
    public void lint() {

    }

    /** -m checkSyntax **/
    @Test
    public void checkSyntax() {

    }

    /** -m docttrLibrary **/
    @Test
    @Ignore
    public void docttrLibrary() {
        String inPath = ROOT + "NamedPizza.ttl";
        String outPath = ROOT + "docttr";
        CLIRunner.run("-m docttrLibrary -f -l " + inPath + " -o " + outPath);

        assertTrue(new File(outPath, "index.html").exists());
    }

}
