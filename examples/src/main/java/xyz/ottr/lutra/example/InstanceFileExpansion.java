package xyz.ottr.lutra.example;

/*-
 * #%L
 * examples
 * %%
 * Copyright (C) 2018 - 2025 University of Oslo
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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.TemplateManager;
import xyz.ottr.lutra.api.StandardFormat;
import xyz.ottr.lutra.api.StandardTemplateManager;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.system.ResultConsumer;
import xyz.ottr.lutra.system.ResultStream;
import xyz.ottr.lutra.wottr.writer.WInstanceWriter;

/**
 * Demonstrates how to read, expand, and write OTTR instance files using the Lutra Java API.
 * Key steps in this example:
 * <ul>
 *   <li>Initialize a {@code StandardTemplateManager} with online template fetching enabled.</li>
 *   <li>Read input instances in different formats.</li>
 *   <li>Optionally, expand instances.</li>
 *   <li>Output the (expanded) instances - in different formats, to file and to console.</li>
 *   <li>Print any messages (warnings or errors) encountered during processing.</li>
 * </ul>
 */

// The code is sectioned into blocks that should be possible to read from top to bottom:
// MAIN, EXAMPLE RUNS, INIT, READ INSTANCES, EXPAND INSTANCES, and OUTPUT INSTANCES.

public class InstanceFileExpansion {

    // The folder where our input files are kept.
    private static final String folder = "examples/src/main/resources/";

    // Main orchestrator class.
    private TemplateManager templateManager;

    // Additional prefixes for pretty-printed output.
    private final Map<String, String> prefixes = Map.of(
            "o-pizza", "http://tpl.ottr.xyz/pizza/0.1/",
            "ex", "http://example.com#"
            );

    public InstanceFileExpansion() {
        initTemplateManager();
    }

    // MAIN
    public static void main(String[] args) {
        InstanceFileExpansion example = new InstanceFileExpansion();
        example.run_expand_and_write_to_file(); // replace this with other run_* methods to run other examples.
    }


    //
    // EXAMPLE RUNS
    //

    /**
     * Read instances, expand, and print expanded instances in RDF format to console.
     * Messages written also written to console.
     */
    public void run_expand_and_print_stdout() {
        ResultStream<Instance> instances = readExampleInstances();
        ResultStream<Instance> expandedInstances = expandInstances(instances);
        printInstances(expandedInstances, StandardFormat.wottr); // wottr here means RDF
    }

    /**
     * Read instances, expand and write expanded instances in RDF format to file. Messages written to console.
     */
    public void run_expand_and_write_to_file() {
        ResultStream<Instance> instances = readExampleInstances();
        ResultStream<Instance> expandedInstances = expandInstances(instances);
        writeInstancesToFile(expandedInstances, StandardFormat.wottr, "output.ttl"); // wottr here means RDF
    }

    /**
     * Read instances and print them in stOTTR format to console.
     */
    public void run_reformat_and_print_stdout() {
        ResultStream<Instance> instances = readExampleInstances();
        printInstances(instances, StandardFormat.stottr);
    }

    /**
     * Read instances, expand them, and write them to a Jena Model. Print model to console. Print messages to console.
     */
    public void run_expand_and_return_rdfmodel() {
        ResultStream<Instance> instances = readExampleInstances();

        // For instructive purposes: this will "just" write the read instances in wOTTR format, which is not what we
        // want in this case; we need to expand the instances!
        // Model unexpandedInstancesModel = getRDFModel(instances);

        ResultStream<Instance> expandedInstances = expandInstances(instances);
        Model model = getRDFModel(expandedInstances);

        model.write(System.out, "TTL");
    }


    //
    // INIT
    //

    /**
     * Initialises a template manager. This class is the central orchestrator in the API. It keeps track of other central
     * classes, such as FormatManager (for reading and writing templates and instances), TemplateStore (for loading and storing
     * templates), and Expanders (for expanding instances).
     *
     * This method sets up a typical template manager for your regular everyday normal instance expansion.
     */
    private void initTemplateManager() {
        // The StandardTemplateManager comes preloaded with the templates in tlp.ottr.xyz and all standard formats
        // (with their readers and writers). For an empty template manager, use TemplateManager().
        templateManager = new StandardTemplateManager();

        // Allow the template manager to fetch templates published online at the template URI.
        templateManager.setFetchMissingDependencies(true);

        // We add some prefixes to prettify output.
        templateManager.getPrefixes().setNsPrefixes(this.prefixes);
    }


    //
    // READ INSTANCES
    //

    /**
     * Reads and parses a set of example instances on different OTTR formats to internal Instance objects.
     *
     * In the method code, Comment in/out files/formats according to your example needs. Notice that instances of all formats are read
     * using the same method calls. The specificity of the input and output of the formats are handled by the readers
     * (and writers) of the Format objects.
     *
     * The returned object is a ResultStream of Instances.
     * A ResultStream is a java.util.Stream of Results.
     * A Result is a java.util.Optional "result" and a "back-trace" of the input used to produce this result. The
     * architecture supports functional programming while collecting error messages generated during the execution.
     * Streams are lazy, i.e., they are only computed when passed to a consumer - in Lutra this is typically
     * a Writer, e.g., an WInstanceWriter.
     * @return A ResultStream of Instances
     */
    private ResultStream<Instance> readExampleInstances() {

        // The instance examples are taken from ottr.xyz. They all contain instances of the same template. This is *not*
        // an important feature of the example. It may result in duplicate instances in output -- or that duplicate instances
        // are truncated if they are collected to a set data structure, such as a Jena Model.
        ResultStream<Instance> stOTTRinstances = readInstances(folder + "pizza-instances.stottr", StandardFormat.stottr);
        ResultStream<Instance> tabOTTRinstances = readInstances(folder + "pizza-instances.xlsx", StandardFormat.tabottr);
        ResultStream<Instance> boOTTRinstances = readInstances(folder + "pizza-map.bottr", StandardFormat.bottr);

        // Combine all read streams to one return argument.
        return ResultStream.concat(List.of(
                stOTTRinstances,
                tabOTTRinstances,
                boOTTRinstances
                ));
    }

    /**
     * Reads instances given in the input file, which needs to be on the given format.
     *
     * @param file path to instance file
     * @param format name of the OTTR format of the instance input file
     * @return a result stream of instances
     */
    private ResultStream<Instance> readInstances(String file, StandardFormat format) {
        return templateManager.readInstances(
                format.name(),
                List.of(file)); // the method expects a list of file/folder names.
    }


    //
    // EXPAND INSTANCES
    //

    /**
     * Expands the input instances into base template instances as specified by the templates loaded in the
     * template store in the template manager.
     *
     * It is possible to code custom expanders.
     * @param instances the instances to be expanded
     * @return a result stream of expanded instances
     */
    private ResultStream<Instance> expandInstances(ResultStream<Instance> instances) {
        // Get template instance expander from template manager.
        Function<Instance, ResultStream<Instance>> expander = templateManager.makeExpander();

        // Apply expander. 'inner' applies the expander function to the Instance "inside" each Result.
        return instances.innerFlatMap(expander);
    }


    //
    // OUTPUT INSTANCES
    //

    /**
     * Prints input instances to standard out and prints any messages (typically errors and warnings) accumulated in
     * the complete pipeline of the input instances (typically: reading, parsing, expanding, writing) also to standard
     * output (unless the messages have already been processed). This method works as a consumer
     * (see java.util.function.Consumer) of the input instances, and will "deplete" the stream of instances, e.g., one
     * stream cannot be written twice.
     * Result consumers typically do not directly return the wanted output, but channels the output it to an output location, such as a
     * file or some output stream. Our consumers typically returns a MessageHandler that holds all the messages
     * accumulated by consuming the stream of results.
     * @param instances the instances to print to standard out
     * @param format the OTTR format for formatting the printed output
     */
    private void printInstances(ResultStream<Instance> instances, StandardFormat format) {
        // Write expanded instances to stdout, and collect any messages generated.
        MessageHandler msgsInstances = templateManager.writeInstances(
                instances,
                format.name(),
                null, // we don't write to file
                System.out);

        // Print any messages. Note that this will print any errors after any output is written.
        msgsInstances.printMessages();
    }

    /**
     * Prints input instances to file and prints any messages.
     * @see this.printInstances
     */
    private void writeInstancesToFile(ResultStream<Instance> instances, StandardFormat format, String filename) {
        MessageHandler msgsInstances = templateManager.writeInstances(
                instances,
                format.name(),
                folder + filename,
                null // we don't write to console.
        );

        // Print any messages. Note that this will print any errors after any output is written.
        msgsInstances.printMessages();
    }

    /**
     * Returns the ottr:Triple base template instances in the input instances as a Jena Model, i.e., RDF graph.
     * The method demonstrates the use of more lower-level classes of the Lutra API. We need to use a WInstanceWriter
     * to write triples to a model.
     * @param instances the instances to be reformatted into a Model
     * @return an RDF Model representation of the input instances
     */
    private Model getRDFModel(ResultStream<Instance> instances) {

        // Set up a Result consumer
        WInstanceWriter instanceWriter = new WInstanceWriter();
        ResultConsumer<Instance> consumer = new ResultConsumer<>(instanceWriter);

        // Apply the consumer to the stream of instances.
        instances.forEach(consumer);

        // Print any messages.
        consumer.getMessageHandler().printMessages();

        // The consumer converts instances to RDF triples, which are stored in a model object in the instanceWriter,
        Model model = instanceWriter.writeToModel();

        // Add prefixes for pretty-printing.
        model.setNsPrefixes(PrefixMapping.Standard);
        model.setNsPrefixes(this.prefixes);

        return model;
    }

}