package xyz.ottr.lutra.cli;

/*-
 * #%L
 * lutra-cli
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

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.apache.jena.shared.PrefixMapping;

import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.io.InstanceWriter;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.io.TemplateWriter;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.TemplateSignature;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.MessageHandler;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultConsumer;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.store.DependencyGraph;
import xyz.ottr.lutra.store.TemplateStore;

import xyz.ottr.lutra.stottr.writer.SInstanceWriter;
import xyz.ottr.lutra.stottr.writer.STemplateWriter;
import xyz.ottr.lutra.wottr.writer.v04.WInstanceWriter;
import xyz.ottr.lutra.wottr.writer.v04.WTemplateWriter;

public class CLI {

    private final Settings settings;
    private final PrintStream outStream;
    //private final PrintStream errStream;

    public CLI(PrintStream outStream) {
        this.settings = new Settings();
        this.outStream = outStream;
        //this.errStream = errStream;
    }

    public CLI() {
        this(System.out);
    }

    public static void main(String[] args) {
        new CLI().run(args);
    }

    public void run(String[] args) {

        CommandLine cli = new CommandLine(settings);
        try {
            cli.parse(args);
        } catch (ParameterException ex) {
            Message err = Message.error(ex.getMessage());
            MessageHandler.printMessage(err);
            return;
        }

        MessageHandler.setQuiet(settings.quiet);

        if (cli.isUsageHelpRequested()) {
            cli.usage(outStream);
        } else if (cli.isVersionHelpRequested()) {
            cli.printVersionHelp(outStream);
        } else if (checkOptions()) {
            execute();
        }
    }

    /**
     * Checks that the provided options form a meaningful execution,
     * otherwise prints an error message.
     */
    private boolean checkOptions() {

        if (settings.inputs.isEmpty()
            && (settings.mode == Settings.Mode.expand
                || settings.mode == Settings.Mode.format)) {

            MessageHandler.printMessage(Message.error("Please provide one or more input files to perform "
                    + settings.mode + " on. For help on usage, use the --help option."));
            return false;
        } else if (settings.library == null
            && (settings.mode == Settings.Mode.expandLibrary
                || settings.mode == Settings.Mode.formatLibrary
                || settings.mode == Settings.Mode.lint)) {

            MessageHandler.printMessage(Message.error("Please provide a library to perform "
                    + settings.mode + " on. For help on usage, use the --help option."));
            return false;
        }
        return true;
    }


    ////////////////////////////////////////////////////////////
    /// MAIN EXECUTION                                       ///
    ////////////////////////////////////////////////////////////


    private void execute() {

        TemplateStore store = new DependencyGraph(ReaderRegistryImpl.getReaderRegistry());
        Result<TemplateReader> successfullReader = parseLibraryInto(store);
        ResultConsumer.use(successfullReader, reader -> {
            PrefixMapping usedPrefixes = OTTR.getDefaultPrefixes();
            usedPrefixes.setNsPrefixes(reader.getPrefixes());
            executeMode(store, usedPrefixes);
        });
    }

    /**
     * Populated store with parsed templates, and returns true if error occurred, and false otherwise.
     */
    private Result<TemplateReader> parseLibraryInto(TemplateStore store) {
        
        store.addOTTRBaseTemplates();

        if (settings.library == null) {
            Collection<TemplateReader> readers = store.getReaderRegistry().getAllTemplateReaders().values();
            return readers.isEmpty() ? Result.empty() : Result.of(readers.iterator().next());
        }
        
        Result<TemplateReader> successfull;
        if (Files.isDirectory(Paths.get(settings.library))) {
            successfull = store.getReaderRegistry().attemptAllReaders(reader ->
                reader.loadTemplatesFromFolder(store, settings.library,
                        settings.extensions, settings.ignoreExtensions));
        } else {
            successfull = store.getReaderRegistry().attemptAllReaders(reader ->
                reader.loadTemplatesFromFile(store, settings.library));
        }
        if (settings.fetchMissingDependencies) {
            MessageHandler msgs = store.fetchMissingDependencies();
            successfull.addMessages(msgs.getMessages());
        }
        
        return successfull;
    } 
    
    private void executeExpand(TemplateStore store, PrefixMapping usedPrefixes) {

        ResultConsumer.use(makeInstanceReader(),
            reader -> {

                ResultConsumer.use(makeExpander(store),
                    expander -> {

                        ResultConsumer.use(makeInstanceWriter(usedPrefixes),
                            writer -> {
                                
                                expandAndWriteInstanes(reader, writer, expander);
                            }
                        );
                    }
                );
            }
        );
    }

    private void executeExpandLibrary(TemplateStore store, PrefixMapping usedPrefixes) {
        
        ResultConsumer.use(store.expandAll(),
            expandedStore -> {

                ResultConsumer.use(makeTemplateWriter(usedPrefixes),
                    writer ->  {

                        writeTemplates(expandedStore, writer);
                    }
                );
            }
        );
    }

    private void executeFormatLibrary(TemplateStore store, PrefixMapping usedPrefixes) {
        
        ResultConsumer.use(makeTemplateWriter(usedPrefixes),
            writer ->  {

                writeTemplates(store, writer);
            }
        );
    }

    private void executeFormat(PrefixMapping usedPrefixes) {
        
        ResultConsumer.use(makeInstanceReader(),
            reader -> {

                ResultConsumer.use(makeInstanceWriter(usedPrefixes),
                    writer ->  {

                        formatInstances(reader, writer);
                    }
                );
            }
        );
    }

    private void executeMode(TemplateStore store, PrefixMapping usedPrefixes) {
        
        int severity = Message.INFO; // Least severe
        if (!settings.quiet) {
            severity = checkTemplates(store);
        }

        if (Message.moreSevere(severity, settings.haltOn)) {
            return;
        }

        switch (settings.mode) {
            case expand:
                executeExpand(store, usedPrefixes);
                break;
            case expandLibrary:
                executeExpandLibrary(store, usedPrefixes);
                break;
            case formatLibrary:
                executeFormatLibrary(store, usedPrefixes);
                break;
            case format:
                executeFormat(usedPrefixes);
                break;
            case lint:
                // Simply load templates and check for messages, as done before the switch
                if (!settings.quiet && Message.moreSevere(Message.WARNING, severity)) {
                    this.outStream.println("No errors found.");
                }
                break;
            default:
                Message err = Message.error("The mode " + settings.mode + " is not yet supported.");
                MessageHandler.printMessage(err);
        } 
    }


    ////////////////////////////////////////////////////////////
    /// MAKER-METHODS, MAKING THINGS BASED ON FLAGS          ///
    ////////////////////////////////////////////////////////////
            
    private Result<InstanceReader> makeInstanceReader() {
        if (settings.inputs.isEmpty()) {
            return Result.empty(Message.error(
                    "No input file provided."));
        }
        return ReaderRegistryImpl.getReaderRegistry().getInstanceReader(settings.inputFormat.toString());
    }

    private Result<Function<Instance, ResultStream<Instance>>> makeExpander(TemplateStore store) {
        if (settings.fetchMissingDependencies) {
            return Result.of(store::expandInstanceFetch);
        } else {
            return Result.of(store::expandInstance);
        }
    }

    private Result<InstanceWriter> makeInstanceWriter(PrefixMapping usedPrefixes) {
        switch (settings.outputFormat) {
            case wottr:
                return Result.of(new WInstanceWriter(usedPrefixes));
            case stottr:
                return Result.of(new SInstanceWriter(usedPrefixes.getNsPrefixMap()));
            default:
                return Result.empty(Message.error(
                        "Output format " + settings.outputFormat.toString()
                            + " not yet supported for instances."));
        }
    }

    private Result<TemplateWriter> makeTemplateWriter(PrefixMapping usedPrefixes) {
        switch (settings.outputFormat) {
            case wottr:
                return Result.of(new WTemplateWriter(usedPrefixes));
            case stottr:
                return Result.of(new STemplateWriter(usedPrefixes.getNsPrefixMap()));
            default:
                return Result.empty(Message.error(
                        "Output format " + settings.outputFormat.toString()
                            + " not yet supported for templates."));
        }
    }


    ////////////////////////////////////////////////////////////
    /// WRITER-METHODS, WRITING THINGS TO FILE               ///
    ////////////////////////////////////////////////////////////

    private void processInstances(Function<String, ResultStream<Instance>> processor,
        InstanceWriter writer) {

        ResultConsumer<Instance> consumer = new ResultConsumer<>(writer);
        ResultStream.innerOf(settings.inputs)
            .innerFlatMap(processor)
            .forEach(consumer);

        if (!Message.moreSevere(consumer.getMessageHandler().printMessages(), settings.haltOn)) {
            writeInstances(writer.write());
        }
    }

    private void formatInstances(InstanceReader reader, InstanceWriter writer) {
        processInstances(reader, writer);
    }

    private void expandAndWriteInstanes(InstanceReader reader, InstanceWriter writer,
        Function<Instance, ResultStream<Instance>> expander) {

        processInstances(ResultStream.innerFlatMapCompose(reader, expander), writer);
    }

    private void writeInstances(String output) {

        // If neither --stdout nor -o is set, default to --stdout
        if (shouldPrintOutput()) {
            this.outStream.println(output);
        }

        if (settings.out == null) {
            return;
        }
        try {
            Files.write(Paths.get(settings.out), output.getBytes(Charset.forName("UTF-8")));
        } catch (IOException ex) {
            if (!settings.quiet) {
                Message err = Message.error(
                    "Error when writing output -- " + ex.getMessage());
                MessageHandler.printMessage(err);
            }
        }
    }

    private void writeTemplates(TemplateStore store, TemplateWriter writer) {
        ResultConsumer<TemplateSignature> consumer = new ResultConsumer<>(writer);
        store.getAllTemplateObjects().forEach(consumer);

        if (!Message.moreSevere(consumer.getMessageHandler().printMessages(), settings.haltOn)) {
            for (String iri : writer.getIRIs()) {
                writeTemplate(iri, writer.write(iri));
            }
        }
    }

    private void writeTemplate(String iri, String output) {

        // If neither --stdout nor -o is set, default to --stdout
        if (shouldPrintOutput()) {
            outStream.println(output);
        }

        if (settings.out == null) {
            return;
        }
        try {
            // TODO: cli-arg to decide extension
            String iriPath = iriToPath(iri);
            Files.createDirectories(Paths.get(settings.out, iriToDirectory(iriPath)));
            Files.write(Paths.get(settings.out, iriPath + getFileSuffix()), output.getBytes(Charset.forName("UTF-8")));
        } catch (IOException | URISyntaxException ex) {
            Message err = Message.error(
                "Error when writing output -- " + ex.getMessage());
            MessageHandler.printMessage(err);
        }
    }


    ////////////////////////////////////////////////////////////
    /// UTILS                                                ///
    ////////////////////////////////////////////////////////////

    private String getFileSuffix() {

        switch (settings.outputFormat) {
            case legacy:
            case wottr:
                return ".ttl";
            case stottr:
                return ".stottr";
            default:
                return "";
        }
    }
        

    private boolean shouldPrintOutput() {
        return settings.stdout || settings.out == null;
    }

    private static String iriToDirectory(String pathStr) {
        Path folder = Paths.get(pathStr).getParent();
        return folder == null ? null : folder.toString();
    }

    private static String iriToPath(String iriStr) throws URISyntaxException {
        return new URI(iriStr).getPath();
    }

    private static int checkTemplates(TemplateStore store) {
        List<Message> msgs = store.checkTemplates();
        msgs.forEach(MessageHandler::printMessage);
        int mostSevere = msgs.stream()
            .mapToInt(Message::getLevel)
            .min()
            .orElse(Message.INFO);
        return mostSevere;
    }
}
