package xyz.ottr.lutra.cli;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

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

        Model expected = RDFDataMgr.loadModel(ROOT + "expected_expand_tabottr.ttl");

        assertThat("should contain the same number of triples", actual.size(), is(expected.size()));
        TestUtils.testIsomorphicModels(actual, expected);

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

    // -o
    // -O
    @Test
    public void expand_stottr_instances_and_write_output_to_file() {
        String args = " "
                + " -l " + ROOT + "person_template.stottr"
                + " -L stottr "
                + ROOT + "person_instances.stottr"
                + " -I stottr "
                + " -o " + ROOT + "expand_stottr.out "
                + " -O wottr ";

        CLIRunner.run(args);

        assertTrue(new File(ROOT + "expand_stottr.out").exists());

        Model actual = RDFDataMgr.loadModel(ROOT + "expand_stottr.out", Lang.TTL);
        Model expected = RDFDataMgr.loadModel(ROOT + "expected_expand_stottr.ttl");

        assertThat(actual.size(), is(expected.size()));
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
