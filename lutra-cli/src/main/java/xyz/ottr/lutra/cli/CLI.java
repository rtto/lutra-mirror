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
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;
import xyz.ottr.lutra.TemplateManager;
import xyz.ottr.lutra.api.StandardFormat;
import xyz.ottr.lutra.api.StandardTemplateManager;
import xyz.ottr.lutra.io.Files;
import xyz.ottr.lutra.io.Format;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.stottr.util.SSyntaxChecker;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;
import xyz.ottr.lutra.wottr.io.RDFIO;

public class CLI {

    private final Settings settings;
    private final PrintStream outStream;
    private final MessageHandler messageHandler;
    private final StandardTemplateManager templateManager;

    public CLI(PrintStream outStream, PrintStream errStream) {
        this.settings = new Settings();
        this.outStream = outStream;
        this.messageHandler = new MessageHandler(errStream);
        this.templateManager = new StandardTemplateManager();
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
                || this.settings.mode == Settings.Mode.format
                || this.settings.mode == Settings.Mode.checkSyntax)) {
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

    private void initTemplateManager() {
        this.templateManager.setFullTrace(this.settings.debugFullTrace);
        this.templateManager.setStackTrace(this.settings.debugStackTrace);
        this.templateManager.setHaltOn(this.settings.haltOn);
        this.templateManager.setFetchMissingDependencies(this.settings.fetchMissingDependencies);
        this.templateManager.setExtensions(this.settings.extensions);
        this.templateManager.setIgnoreExtensions(this.settings.ignoreExtensions);
    }

    private void initLibrary() {

        if (initStandardLibrary().isGreaterEqualThan(this.settings.haltOn)) {
            return;
        }
        if (parseLibrary().isGreaterEqualThan(this.settings.haltOn)) {
            return;
        }
        if (parsePrefixes().isGreaterEqualThan(this.settings.haltOn)) {
            return;
        }
        if (checkLibrary().isGreaterEqualThan(this.settings.haltOn)) {
            return;
        }
    }

    private void execute() {

        initTemplateManager();

        if (this.settings.mode == Settings.Mode.checkSyntax) {
            executeCheckSyntax();
        } else {

            initLibrary();

            switch (this.settings.mode) {
                case expand:
                    executeExpand();
                    break;
                case expandLibrary:
                    executeExpandLibrary();
                    break;
                case formatLibrary:
                    executeFormatLibrary();
                    break;
                case format:
                    executeFormat();
                    break;
                case lint:
                    break;
                default:
                    Message err = Message.error("The mode " + this.settings.mode + " is not yet supported.");
                    this.messageHandler.printMessage(err);
            }
        }
    }

    private void executeCheckSyntax() {

        if (this.settings.inputFormat == StandardFormat.stottr) {

            for (String file : this.settings.inputs) {
                this.outStream.println("Checking file: " + file);
                var checker = new SSyntaxChecker(this.messageHandler);
                try {
                    checker.checkFile(Paths.get(file));
                    this.messageHandler.printMessages();
                } catch (IOException e) {
                    this.outStream.println("Error checking file.");
                    e.printStackTrace(this.outStream);
                }
            }
        } else {
            this.outStream.println("Unsupported format " + this.settings.inputFormat);
        }
    }

    private void executeExpand() {
        writeInstances(parseAndExpandInstances());
    }

    private void executeFormat() {
        writeInstances(parseInstances());
    }

    private void executeExpandLibrary() {
        this.messageHandler.use(this.templateManager.expandStore(), this::writeTemplates);
    }

    private void executeFormatLibrary() {
        writeTemplates(this.templateManager);
    }

    private Message.Severity checkLibrary() {
        MessageHandler msgs = this.templateManager.checkTemplates();
        Message.Severity severity = this.settings.quiet ? msgs.getMostSevere() : msgs.printMessages();

        if (this.settings.mode == Settings.Mode.lint
            && !this.settings.quiet
            && severity.isLessThan(Message.Severity.WARNING)) {

            // Print message if linting and no errors found
            this.outStream.println("No errors found.");
        }
        return severity;
    }

    ////////////////////////////////////////////////////////////
    /// Parsing and writing                                  ///
    ////////////////////////////////////////////////////////////


    private Message.Severity initStandardLibrary() {
        // Load standard library
        var msgs = this.templateManager.loadStandardTemplateLibrary();
        this.messageHandler.combine(msgs); // Use this.messageHandler's settings
        return this.messageHandler.printMessages();
    }

    private Message.Severity parseLibrary() {

        if (this.settings.library == null || this.settings.library.length == 0) {
            return Message.Severity.least();
        }

        Format libraryFormat = this.settings.libraryFormat == null
                ? null
                : this.templateManager.getFormat(this.settings.libraryFormat.toString());

        return this.templateManager.readLibrary(libraryFormat, this.settings.library)
            .printMessages();
    }

    private Message.Severity parsePrefixes() {
        if (!StringUtils.isNotBlank(this.settings.prefixes)) {
            return Message.Severity.least();
        }
        Result<Model> userPrefixes = RDFIO.fileReader().parse(this.settings.prefixes);
        return this.messageHandler.use(userPrefixes, up -> this.templateManager.addPrefixes(up));
    }

    public ResultStream<Instance> parseInstances() {
        Format inFormat = this.templateManager.getFormat(this.settings.inputFormat.toString());
        return this.templateManager.readInstances(inFormat, this.settings.inputs);
    }

    public ResultStream<Instance> parseAndExpandInstances() {
        return parseInstances().innerFlatMap(this.templateManager.makeExpander());
    }

    private void writeInstances(ResultStream<Instance> ins) {

        Format outFormat = this.templateManager.getFormat(this.settings.outputFormat.toString());
        this.templateManager
            .writeInstances(ins, outFormat, makeInstanceWriter(outFormat.getDefaultFileSuffix()))
            .printMessages();
    }

    private void writeTemplates(TemplateManager templateManager) {
        Format outFormat = this.templateManager.getFormat(this.settings.outputFormat.toString());
        templateManager.writeTemplates(outFormat, makeTemplateWriter(outFormat.getDefaultFileSuffix()));
    }

    private Function<String, Optional<Message>> makeInstanceWriter(String suffix) {
        return str -> {
            if (shouldPrintOutput()) {
                this.outStream.println(str);
            }
            if (this.settings.out != null) {
                return Files.writeInstancesTo(str, suffix, this.settings.out);
            }
            return Optional.empty();
        };
    }

    private BiFunction<String, String, Optional<Message>> makeTemplateWriter(String suffix) {
        return (iri, str) -> {
            if (shouldPrintOutput()) {
                this.outStream.println(str);
            }
            if (this.settings.out != null) {
                return Files.writeTemplatesTo(iri, str, suffix, this.settings.out);
            }
            return Optional.empty();
        };
    }

    private boolean shouldPrintOutput() {
        return this.settings.stdout || this.settings.out == null;
    }
}
