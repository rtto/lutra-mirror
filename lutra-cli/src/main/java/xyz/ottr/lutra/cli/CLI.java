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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.jena.shared.PrefixMapping;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.store.graph.DependencyGraph;
import xyz.ottr.lutra.stottr.writer.SInstanceWriter;
import xyz.ottr.lutra.stottr.writer.STemplateWriter;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultConsumer;
import xyz.ottr.lutra.system.ResultStream;
import xyz.ottr.lutra.wottr.writer.WInstanceWriter;
import xyz.ottr.lutra.wottr.writer.WTemplateWriter;
import xyz.ottr.lutra.writer.InstanceWriter;
import xyz.ottr.lutra.writer.TemplateWriter;

public class CLI {

    private final Settings settings;
    private final PrintStream outStream;
    private final PrintStream errStream;
    private final MessageHandler messageHandler;

    public CLI(PrintStream outStream, PrintStream errStream) {
        this.settings = new Settings();
        this.outStream = outStream;
        this.errStream = errStream;
        this.messageHandler = new MessageHandler(errStream);
    }

    public CLI() {
        this(System.out, System.err);
    }

    public static void main(String[] args) {
        new CLI().run(args);
    }

    public void run(String[] args) {

        CommandLine cli = new CommandLine(this.settings);
        try {
            cli.parse(args);
        } catch (ParameterException ex) {
            Message err = Message.error(ex.getMessage());
            this.messageHandler.printMessage(err);
            return;
        }

        this.messageHandler.setQuiet(this.settings.quiet);

        if (cli.isUsageHelpRequested()) {
            cli.usage(this.outStream);
        } else if (cli.isVersionHelpRequested()) {
            cli.printVersionHelp(this.outStream);
        } else if (checkOptions()) {
            execute();
        }
    }

    /**
     * Checks that the provided options form a meaningful execution,
     * otherwise prints an error message.
     */
    private boolean checkOptions() {

        if (this.settings.inputs.isEmpty()
            && (this.settings.mode == Settings.Mode.expand
                || this.settings.mode == Settings.Mode.format)) {

            this.messageHandler.printMessage(Message.error("Must provide one or more input files. "
                + "For help on usage, use the --help option."));
            return false;
        } else if (this.settings.library == null
            && (this.settings.mode == Settings.Mode.expandLibrary
                || this.settings.mode == Settings.Mode.formatLibrary
                || this.settings.mode == Settings.Mode.lint)) {

            this.messageHandler.printMessage(Message.error("Must provide a library. "
                + "For help on usage, use the --help option."));
            return false;
        }
        return true;
    }


    ////////////////////////////////////////////////////////////
    /// MAIN EXECUTION                                       ///
    ////////////////////////////////////////////////////////////


    private void execute() {

        TemplateStore store = new DependencyGraph(ReaderRegistryImpl.getReaderRegistry());
        Result<Map<String, String>> prefixesRes = parseLibraryInto(store);
        this.messageHandler.use(prefixesRes, prefixes -> {
            PrefixMapping usedPrefixes = OTTR.getDefaultPrefixes();
            usedPrefixes.setNsPrefixes(prefixes);
            executeMode(store, usedPrefixes);
        });
    }

    /**
     * Populated store with parsed templates, and returns true if error occurred, and false otherwise.
     */
    private Result<Map<String, String>> parseLibraryInto(TemplateStore store) {
        
        store.addOTTRBaseTemplates();

        if (this.settings.library == null || this.settings.library.length == 0) {
            return Result.of(OTTR.getDefaultPrefixes().getNsPrefixMap());
        }

        Result<Map<String, String>> prefixes = Result.of(new HashMap<>());

        for (int i = 0; i < this.settings.library.length; i++) {
            // check if library is folder or file, and get readerFunction accordingly:
            String lib = this.settings.library[i];

            Function<TemplateReader, MessageHandler> readerFunction =
                Files.isDirectory(Paths.get(this.settings.library[i]))
                    ? reader -> reader.loadTemplatesFromFolder(store, lib,
                    this.settings.extensions, this.settings.ignoreExtensions)
                    : reader -> reader.loadTemplatesFromFile(store, lib);

            Result<TemplateReader> reader;
            // check if libraryFormat is set or not
            if (this.settings.libraryFormat != null) {
                reader = store.getReaderRegistry().getTemplateReaders(this.settings.libraryFormat.toString());
                reader.map(readerFunction)
                    .map(mgs -> mgs.toSingleMessage("Attempt of parsing templates as "
                            + this.settings.libraryFormat + " format failed:"))
                    .ifPresent(mgs -> mgs.ifPresent(reader::addMessage));
                prefixes.addResult(reader, (m, r) -> m.putAll(r.getPrefixes()));
            } else {
                reader = store.getReaderRegistry().attemptAllReaders(readerFunction);
            }
            prefixes.addResult(reader, (m, r) -> m.putAll(r.getPrefixes()));
        }

        if (this.settings.fetchMissingDependencies) {
            MessageHandler msgs = store.fetchMissingDependencies();
            prefixes.addMessages(msgs.getMessages());
        }
        
        return prefixes;
    } 
    
    private void executeExpand(TemplateStore store, PrefixMapping usedPrefixes) {

        this.messageHandler.use(makeInstanceReader(),
            reader -> {

                this.messageHandler.use(makeExpander(store),
                    expander -> {

                        this.messageHandler.use(makeInstanceWriter(usedPrefixes),
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
        
        this.messageHandler.use(store.expandAll(),
            expandedStore -> {

                this.messageHandler.use(makeTemplateWriter(usedPrefixes),
                    writer ->  {

                        writeTemplates(expandedStore, writer);
                    }
                );
            }
        );
    }

    private void executeFormatLibrary(TemplateStore store, PrefixMapping usedPrefixes) {
        
        this.messageHandler.use(makeTemplateWriter(usedPrefixes),
            writer ->  {

                writeTemplates(store, writer);
            }
        );
    }

    private void executeFormat(PrefixMapping usedPrefixes) {
        
        this.messageHandler.use(makeInstanceReader(),
            reader -> {

                this.messageHandler.use(makeInstanceWriter(usedPrefixes),
                    writer ->  {

                        formatInstances(reader, writer);
                    }
                );
            }
        );
    }

    private void executeMode(TemplateStore store, PrefixMapping usedPrefixes) {
        
        int severity = Message.INFO; // Least severe
        if (!this.settings.quiet) {
            severity = checkTemplates(store);
        }

        if (Message.moreSevere(severity, this.settings.haltOn)) {
            return;
        }

        switch (this.settings.mode) {
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
                if (!this.settings.quiet && Message.moreSevere(Message.WARNING, severity)) {
                    this.outStream.println("No errors found.");
                }
                break;
            default:
                Message err = Message.error("The mode " + this.settings.mode + " is not yet supported.");
                this.messageHandler.printMessage(err);
        } 
    }


    ////////////////////////////////////////////////////////////
    /// MAKER-METHODS, MAKING THINGS BASED ON FLAGS          ///
    ////////////////////////////////////////////////////////////
            
    private Result<InstanceReader> makeInstanceReader() {
        if (this.settings.inputs.isEmpty()) {
            return Result.error("No input file provided.");
        }
        return ReaderRegistryImpl.getReaderRegistry().getInstanceReader(this.settings.inputFormat.toString());
    }

    private Result<Function<Instance, ResultStream<Instance>>> makeExpander(TemplateStore store) {
        if (this.settings.fetchMissingDependencies) {
            return Result.of(store::expandInstanceFetch);
        } else {
            return Result.of(store::expandInstance);
        }
    }

    private Result<InstanceWriter> makeInstanceWriter(PrefixMapping usedPrefixes) {
        switch (this.settings.outputFormat) {
            case wottr:
                return Result.of(new WInstanceWriter(usedPrefixes));
            case stottr:
                return Result.of(new SInstanceWriter(usedPrefixes.getNsPrefixMap()));
            default:
                return Result.error("Output format " + this.settings.outputFormat + " not (yet?) supported for instances.");
        }
    }

    private Result<TemplateWriter> makeTemplateWriter(PrefixMapping usedPrefixes) {
        switch (this.settings.outputFormat) {
            case wottr:
                return Result.of(new WTemplateWriter(usedPrefixes));
            case stottr:
                return Result.of(new STemplateWriter(usedPrefixes.getNsPrefixMap()));
            default:
                return Result.error("Output format " + this.settings.outputFormat + " not (yet?) supported for templates.");
        }
    }


    ////////////////////////////////////////////////////////////
    /// WRITER-METHODS, WRITING THINGS TO FILE               ///
    ////////////////////////////////////////////////////////////

    private void processInstances(Function<String, ResultStream<Instance>> processor, InstanceWriter writer) {

        ResultConsumer<Instance> consumer = new ResultConsumer<>(writer, this.errStream);
        ResultStream.innerOf(this.settings.inputs)
            .innerFlatMap(processor)
            .forEach(consumer);

        if (!Message.moreSevere(consumer.getMessageHandler().printMessages(), this.settings.haltOn)) {
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

        if (this.settings.out == null) {
            return;
        }
        try {
            Files.write(Paths.get(this.settings.out), output.getBytes(Charset.forName("UTF-8")));
        } catch (IOException ex) {
            if (!this.settings.quiet) {
                Message err = Message.error("Error writing output: " + ex.getMessage());
                this.messageHandler.printMessage(err);
            }
        }
    }

    private void writeTemplates(TemplateStore store, TemplateWriter writer) {
        ResultConsumer<Signature> consumer = new ResultConsumer<>(writer, this.errStream);
        store.getAllTemplateObjects().forEach(consumer);

        if (!Message.moreSevere(consumer.getMessageHandler().printMessages(), this.settings.haltOn)) {
            for (String iri : writer.getIRIs()) {
                writeTemplate(iri, writer.write(iri));
            }
        }
    }

    private void writeTemplate(String iri, String output) {

        // If neither --stdout nor -o is set, default to --stdout
        if (shouldPrintOutput()) {
            this.outStream.println(output);
        }

        if (this.settings.out == null) {
            return;
        }
        try {
            // TODO: cli-arg to decide extension
            String iriPath = iriToPath(iri);
            Files.createDirectories(Paths.get(this.settings.out, iriToDirectory(iriPath)));
            Files.write(Paths.get(this.settings.out, iriPath + getFileSuffix()), output.getBytes(Charset.forName("UTF-8")));
        } catch (IOException | URISyntaxException ex) {
            Message err = Message.error(
                "Error when writing output -- " + ex.getMessage());
            this.messageHandler.printMessage(err);
        }
    }


    ////////////////////////////////////////////////////////////
    /// UTILS                                                ///
    ////////////////////////////////////////////////////////////

    private String getFileSuffix() {

        switch (this.settings.outputFormat) {
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
        return this.settings.stdout || this.settings.out == null;
    }

    private static String iriToDirectory(String pathStr) {
        Path folder = Paths.get(pathStr).getParent();
        return folder == null ? null : folder.toString();
    }

    private static String iriToPath(String iriStr) throws URISyntaxException {
        return new URI(iriStr).getPath();
    }

    private int checkTemplates(TemplateStore store) {
        List<Message> msgs = store.checkTemplates();
        msgs.forEach(this.messageHandler::printMessage);
        int mostSevere = msgs.stream()
            .mapToInt(Message::getLevel)
            .min()
            .orElse(Message.INFO);
        return mostSevere;
    }
}
