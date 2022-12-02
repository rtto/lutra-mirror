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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.wottr.io.RDFIO;

public class ExpandTest {
    private static final String ROOT = "src/test/resources/expand/";

    // -I stottr
    @Test
    public void expand_stottr_instances() {
        String args = " -f "
                + "-L stottr -l "
                + ROOT + "templates/device_template.stottr"
                + " -I stottr "
                + ROOT + "instances/device_instances.stottr";

        CLI cli = new CLI();
        MessageHandler msgHandler = cli.getMessageHandler();
        cli.executeArgs(args.trim().split("\\s+"));
        Assertions.noErrors(msgHandler);

    }

    // -I wottr
    @Test
    public void expand_wottr_instances() {
        String args = " -f "
                + ROOT + "instances/pizza_instances.wottr"
                + " -I wottr "
                + " -o " + ROOT + "output.out";

        CLIRunner.run(args);

        Model actual = RDFIO.fileReader().parse(ROOT + "output.out").get();
        Model expected = RDFIO.fileReader().parse(ROOT + "expected/expand_pizza.ttl").get();
        TestUtils.testIsomorphicModels(actual, expected);
    }

    // -I stottr
    // variables in list
    @Test
    public void stottr_template_with_variables_in_list() {
        String args = "  -f -L stottr -l "
                + ROOT + "templates/variables_in_list.stottr "
                + ROOT + "instances/variables_in_list_instances.stottr "
                + " -I stottr "
                + " -o " + ROOT + "output.out";

        CLIRunner.run(args);

        Model actual = RDFIO.fileReader().parse(ROOT + "output.out").get();
        Model expected = RDFIO.fileReader().parse(ROOT + "expected/variables_in_list.ttl").get();
        TestUtils.testIsomorphicModels(actual, expected);
    }

    // -I wottr
    // variables in list
    @Test
    public void wottr_template_with_variables_in_list() {
        String args = "  -f -L wottr -l "
                + ROOT + "templates/variables_in_list.wottr "
                + ROOT + "instances/variables_in_list_instances.wottr "
                + " -I wottr "
                + " -o " + ROOT + "output.out";

        CLIRunner.run(args);

        Model actual = RDFIO.fileReader().parse(ROOT + "output.out").get();
        Model expected = RDFIO.fileReader().parse(ROOT + "expected/variables_in_list.ttl").get();
        TestUtils.testIsomorphicModels(actual, expected);
    }

    // -I tabottr
    @Test
    public void expand_tabottr_instances() {
        String args = "-I tabottr -f --stdout " + ROOT + "instances/NamedPizza-instances.xlsx "
                + " -o " + ROOT + "output.out";

        CLIRunner.run(args);

        //TODO: make expected output file
        //Model actual = RDFIO.fileReader().parse(ROOT + "output.out").get();
        //Model expected = RDFIO.fileReader().parse("").get();
        //TestUtils.testIsomorphicModels(actual, expected);
    }

    @Test
    public void expand_tabottr_instances2() {
        String args = "-I tabottr -f --stdout " + ROOT + "instances/PizzaOntology-instances.xlsx "
                + " -o " + ROOT + "output.out";

        CLIRunner.run(args);

        //TODO: make expected output file
        //Model actual = RDFIO.fileReader().parse(ROOT + "output.out").get();
        //Model expected = RDFIO.fileReader().parse("").get();
        //TestUtils.testIsomorphicModels(actual, expected);
    }

    // -I bottr
    @Test
    public void expand_bottr_RDFSource() {
        String bottrRoot = "../lutra-bottr/src/test/resources/maps/";

        String args = "-I bottr -f "
                + bottrRoot + "instanceMapRDFSource.ttl "
                + " -o " + ROOT + "output.out";

        CLIRunner.run(args);

        Model actual = RDFIO.fileReader().parse(ROOT + "output.out").get();
        Model expected = RDFIO.fileReader().parse(ROOT + "expected/expand_bottr_RDFSource.ttl").get();
        TestUtils.testIsomorphicModels(actual, expected);
    }

    @Disabled("Relative source paths inside query don't work.")
    @Test
    public void expand_bottr_H2Source() {
        String bottrRoot = "../lutra-bottr/src/test/resources/maps/";

        String args = "-I bottr -f --stdout "
                + bottrRoot + "instanceMapH2Source.ttl";

        CLIRunner.run(args);

    }

    @Test
    public void expand_bottr_SPARQLSource() {
        String bottrRoot = "../lutra-bottr/src/test/resources/maps/";

        String args = "-I bottr -f "
                + bottrRoot + "instanceMapSPARQL.ttl "
                + " -o " + ROOT + "output.out";

        CLIRunner.run(args);

        Model actual = RDFIO.fileReader().parse(ROOT + "output.out").get();
        assertThat("should contain the same number of triples", actual.size(), is(13L));
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

        CLIRunner.run("--mode expand --inputFormat stottr --fetchMissing " + ROOT + "instances/pizza_instances.stottr");

        // Restore old out
        stdOut.flush();
        System.setOut(stdOut);

        // parse captured console output to model
        Model actual = ModelFactory.createDefaultModel();
        RDFDataMgr.read(actual, new StringReader(baos.toString(StandardCharsets.UTF_8)), "", Lang.TURTLE);

        Model expected = RDFDataMgr.loadModel(ROOT + "expected/expand_pizza.ttl");

        TestUtils.testIsomorphicModels(actual, expected);
    }

}
