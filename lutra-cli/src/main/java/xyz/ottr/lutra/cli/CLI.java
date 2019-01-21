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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

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

import xyz.ottr.lutra.tabottr.io.TabInstanceParser;
import xyz.ottr.lutra.wottr.WTemplateFactory;
import xyz.ottr.lutra.wottr.io.WFileReader;
import xyz.ottr.lutra.wottr.io.WInstanceParser;
import xyz.ottr.lutra.wottr.io.WInstanceWriter;
import xyz.ottr.lutra.wottr.io.WTemplateParser;
import xyz.ottr.lutra.wottr.io.WTemplateWriter;

public class CLI {

    private static Settings settings;

    public static void main(String[] args) {

        settings = new Settings();
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
            cli.usage(System.out);
        } else if (cli.isVersionHelpRequested()) {
            cli.printVersionHelp(System.out);
        } else if (checkOptions()) {
            execute();
        }
    }

    /**
     * Checks that the provided options form a meaningful execution,
     * otherwise prints an error message.
     */
    private static boolean checkOptions() {

        if (settings.input == null
            && (settings.mode == Settings.Mode.expand
                || settings.mode == Settings.Mode.format)) {

            MessageHandler.printMessage(Message.error("Please provide an input file to perform "
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


    private static void execute() {

        //if (settings.library == null && !settings.fetchMissingDependencies) {
        //    if (!settings.quiet) {
        //        Message err = Message.error(
        //            "No template library provided and not set to fetch missing templates, "
        //                + "thus nothing can be done.");
        //        MessageHandler.printMessage(err);
        //    }
        //    return;
        //}
        TemplateStore store = new DependencyGraph(); // TODO: implementation choice based on cli-arg
        ResultConsumer.use(makeTemplateReader(),
            reader -> {

                ResultConsumer.use(parseLibraryInto(reader, store),
                    messageHandler -> {

                        // TODO: cli-arg to decide if continue, int-flag to denote ignore level
                        if (!Message.moreSevere(messageHandler.printMessages(), settings.ignore)) {
                            executeMode(store);
                        }
                    }
                );
            }
        );
    }

    /**
     * Populated store with parsed templates, and returns true if error occured, and false otherwise.
     */
    private static Result<MessageHandler> parseLibraryInto(TemplateReader reader, TemplateStore store) {

        // TODO: Make cli-argument of both base template and suffixes to include/ignore
        store.addTemplateSignature(WTemplateFactory.createTripleTemplateHead());

        if (settings.library == null) {
            return Result.of(new MessageHandler());
        }

        Result<MessageHandler> consumer;
        try {
            consumer = Result.of(reader.loadTemplatesFromFolder(store, settings.library,
                    settings.endings, settings.ignoreEndings));
        } catch (IOException ex) {
            Message err = Message.error(
                "Error when parsing templates from folder -- " + ex.getMessage());
            return Result.empty(err);
        }
        if (settings.fetchMissingDependencies) {
            consumer = consumer.map(cnsmr ->
                cnsmr.combine(store.fetchMissingDependencies(reader)));
        }

        return consumer;
    } 

    private static void executeMode(TemplateStore store) {
        
        checkTemplates(store);
        switch (settings.mode) {
            case expand:
                ResultConsumer.use(makeInstanceReader(),
                    reader -> {

                        ResultConsumer.use(makeExpander(store),
                            expander -> {

                                ResultConsumer.use(makeInstanceWriter(),
                                    writer -> {

                                        expandAndWriteInstanes(reader, writer, expander);
                                    }
                                );
                            }
                        );
                    }
                );
                break;
            case expandLibrary:
                ResultConsumer.use(store.expandAll(),
                    expandedStore -> {

                        ResultConsumer.use(makeTemplateWriter(),
                            writer ->  {

                                writeTemplates(expandedStore, writer);
                            }
                        );
                    }
                );
                break;
            case formatLibrary:
                ResultConsumer.use(makeTemplateWriter(),
                    writer ->  {

                        writeTemplates(store, writer);
                    }
                );
                break;
            case format:
                ResultConsumer.use(makeInstanceReader(),
                    reader -> {

                        ResultConsumer.use(makeInstanceWriter(),
                            writer ->  {

                                formatInstances(reader, writer);
                            }
                        );
                    }
                );
                break;
            case lint:
                // Simply load templates and check for messages, as done before the switch
                break;
            default:
                if (!settings.quiet) {
                    Message err = Message.error("The mode " + settings.mode + " is not yet supported.");
                    MessageHandler.printMessage(err);
                }
        } 
    }


    ////////////////////////////////////////////////////////////
    /// MAKER-METHODS, MAKING THINGS BASED ON FLAGS          ///
    ////////////////////////////////////////////////////////////


    private static Result<TemplateReader> makeTemplateReader() {
        switch (settings.libraryFormat) {
            case stottr:
                return Result.empty(Message.error(
                        "stOTTR not yet supported as input format."));
            case tabottr:
                return Result.empty(Message.error(
                        "TabOTTR does not support template definitions."));
            case qottr:
                return Result.empty(Message.error(
                        "qOTTR does not support template definitions."));
            case legacy:
                // legacy WOTTR
                return Result.of(new TemplateReader(new WFileReader(),
                        new xyz.ottr.lutra.wottr.legacy.io.WTemplateParser()));
            default:
                // WOTTR
                return Result.of(new TemplateReader(new WFileReader(), new WTemplateParser()));
        }
    }
            
    private static Result<InstanceReader> makeInstanceReader() {
        if (settings.input == null) {
            return Result.empty(Message.error(
                    "No input file provided."));
        }
        switch (settings.inputFormat) {
            case tabottr:
                return Result.of(new InstanceReader(new TabInstanceParser()));
            case stottr:
                return Result.empty(Message.error(
                        "stOTTR not yet supported as input format."));
            case qottr:
                return Result.empty(Message.error(
                        "qOTTR not yet supported as input format."));
            case legacy:
                // legacy WOTTR
                return Result.of(new InstanceReader(new WFileReader(),
                        new xyz.ottr.lutra.wottr.legacy.io.WInstanceParser()));
            default: // WOTTR
                return Result.of(new InstanceReader(new WFileReader(), new WInstanceParser()));
        }
    }

    private static Result<Function<Instance, ResultStream<Instance>>> makeExpander(TemplateStore store) {
        if (settings.fetchMissingDependencies) {
            Function<TemplateReader, Function<Instance, ResultStream<Instance>>> fun =
                reader -> ins -> store.expandInstance(ins, reader);
            return makeTemplateReader().map(fun);
        } else {
            return Result.of(ins -> store.expandInstance(ins));
        }
    }

    private static Result<InstanceWriter> makeInstanceWriter() {
        switch (settings.outputFormat) {
            case tabottr:
                return Result.empty(Message.error(
                        "tabOTTR not yet supported as output format."));
            case stottr:
                return Result.empty(Message.error(
                        "stOTTR not yet supported as output format."));
            case qottr:
                return Result.empty(Message.error(
                        "qOTTR is yet not supported as output format."));
            default:
                // WOTTR
                return Result.of(new WInstanceWriter());
        }
    }

    private static Result<TemplateWriter> makeTemplateWriter() {
        switch (settings.outputFormat) {
            case tabottr:
                return Result.empty(Message.error(
                        "tabOTTR not yet supported as output format."));
            case stottr:
                return Result.empty(Message.error(
                        "stOTTR not yet supported as output format."));
            case qottr:
                return Result.empty(Message.error(
                        "qOTTR is yet not supported as output format."));
            default:
                // WOTTR
                return Result.of(new WTemplateWriter());
        }
    }


    ////////////////////////////////////////////////////////////
    /// WRITER-METHODS, WRITING THINGS TO FILE               ///
    ////////////////////////////////////////////////////////////

    private static void formatInstances(InstanceReader reader, InstanceWriter writer) {
        
        ResultConsumer<Instance> consumer = new ResultConsumer<>(writer);
        reader.apply(settings.input)
            .forEach(consumer);

        if (!Message.moreSevere(consumer.getMessageHandler().printMessages(), settings.ignore)) {
            writeInstances(writer.write());
        }
    }

    private static void expandAndWriteInstanes(InstanceReader reader, InstanceWriter writer,
        Function<Instance, ResultStream<Instance>> expander) {

        ResultConsumer<Instance> consumer = new ResultConsumer<>(writer);
        reader.apply(settings.input)
            .innerFlatMap(expander)
            .forEach(consumer);

        if (!Message.moreSevere(consumer.getMessageHandler().printMessages(), settings.ignore)) {
            writeInstances(writer.write());
        }
    }

    private static void writeInstances(String output) {

        if (settings.stdout) {
            System.out.println(output);
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

    private static void writeTemplates(TemplateStore store, TemplateWriter writer) {
        ResultConsumer<TemplateSignature> consumer = new ResultConsumer<>(writer);
        store.getAllTemplateObjects().forEach(consumer);

        if (!Message.moreSevere(consumer.getMessageHandler().printMessages(), settings.ignore)) {
            for (String iri : writer.getIRIs()) {
                writeTemplate(iri, writer.write(iri));
            }
        }
    }

    private static void writeTemplate(String iri, String output) {

        if (settings.stdout) {
            System.out.println(output);
        }

        if (settings.out == null) {
            return;
        }
        try {
            // TODO: cli-arg to decide ending
            String iriPath = iriToPath(iri);
            Files.createDirectories(Paths.get(settings.out, iriToDirectory(iriPath)));
            Files.write(Paths.get(settings.out, iriPath + ".ttl"), output.getBytes(Charset.forName("UTF-8")));
        } catch (IOException ex) {
            Message err = Message.error(
                "Error when writing output -- " + ex.getMessage());
            MessageHandler.printMessage(err);
        } catch (URISyntaxException ex) {
            Message err = Message.error(
                "Error when writing output -- " + ex.getMessage());
            MessageHandler.printMessage(err);
        }
    }


    ////////////////////////////////////////////////////////////
    /// UTILS                                                ///
    ////////////////////////////////////////////////////////////


    private static String iriToDirectory(String pathStr) throws URISyntaxException {
        Path folder = Paths.get(pathStr).getParent();
        return folder == null ? null : folder.toString();
    }

    private static String iriToPath(String iriStr) throws URISyntaxException {
        return new URI(iriStr).getPath();
    }

    private static void checkTemplates(TemplateStore store) {
        if (!settings.quiet) {
            store.checkTemplates().forEach(msg -> MessageHandler.printMessage(msg));
        }
    }

}
