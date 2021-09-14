package xyz.ottr.lutra;



/*-
 * #%L
 * lutra-core
 * %%
 * Copyright (C) 2018 - 2020 University of Oslo
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

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.Getter;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.io.Format;
import xyz.ottr.lutra.io.FormatManager;
import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.store.Expander;
import xyz.ottr.lutra.store.StandardTemplateStore;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.store.expansion.CheckingExpander;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultConsumer;
import xyz.ottr.lutra.system.ResultStream;
import xyz.ottr.lutra.system.Trace;
import xyz.ottr.lutra.writer.InstanceWriter;
import xyz.ottr.lutra.writer.TemplateWriter;

public class TemplateManager {

    private final Settings settings;
    @Getter private final PrefixMapping prefixes;
    @Getter private final FormatManager formatManager;
    @Getter private final TemplateStore templateStore;
    
    private TemplateManager(Settings settings, PrefixMapping prefixes, FormatManager formatManager, TemplateStore templateStore) {
        this.templateStore = templateStore;
        this.formatManager = formatManager;
        this.settings = settings;
        this.prefixes = prefixes;
        Trace.setDeepTrace(this.settings.deepTrace);
        Message.setPrintStackTrace(this.settings.stackTrace);
    }

    private TemplateManager(FormatManager formatManager, TemplateStore templateStore) {
        this(new Settings(), PrefixMapping.Factory.create().setNsPrefixes(OTTR.getDefaultPrefixes()),
            formatManager, templateStore);
    }
    
    /**
     * Creates a new StandardTemplateStore using the argument TemplateStore
     * for all Template related operations. This's FormatManager will
     * be gotten from the argument TemplateStore.
     * 
     * @param store
     *      The TemplateStore to use for all Template-related operations.
     */
    public TemplateManager(TemplateStore store) {
        this(store.getFormatManager(), store);
    }
    
    /**
     * Creates a new StandardTemplateStore using the argument FormatManager
     * to retrieve Formats. A new TemplateStore used for all Template operations
     * will be created using the argument FormatManager.
     * 
     * @param formatManager
     *      The FormatManager to use to retrieve Formats
     */
    public TemplateManager(FormatManager formatManager) {
        this(formatManager, makeDefaultStore(formatManager));
    }
    
    /**
     * Creates a new StandardTemplateStore creating a new FormatManager
     * to retrieve Formats. A new TemplateStore used for all Template operations
     * will be created using that FormatManager.
     */
    public TemplateManager() {
        this(new FormatManager());
    }
    
    /**
     * This toggles tracing such that printed messages get a stack trace
     * giving more information on the location of the concerned objects.
     * NB! Enabling this flag will deteriorate performance.
     *
     * @param enable
     *      True enables deep trace, false disables deep trace.
     */
    public void setFullTrace(boolean enable) {
        this.settings.deepTrace = enable;
        Trace.setDeepTrace(enable);
    }

    public void setStackTrace(boolean enable) {
        this.settings.stackTrace = enable;
        Message.setPrintStackTrace(enable);
    }
    
    /**
     * Fetch missing template dependencies. It is here assumed that
     * templates' definitions are accessible via their IRI, that is, the IRI is
     * either a path to a file, a URL, or similar.
     * 
     * @param enable
     *      True enables fetching, false disables fetching.
     */
    public void setFetchMissingDependencies(boolean enable) {
        this.settings.fetchMissingDependencies = enable;
    }

    /**
     * Halt on messages with a severity equal to or below the argument.
     */
    /*public void setHaltOn(Message.Severity severity) {
        this.settings.haltOn = severity;
    }
    */

    /**
     * Sets file extension of files to use as input to template library.
     * 
     * @param ext
     *     An Array of Strings denoting file extensions to use as input when parsing
     *     a Template library.
     */
    public void setExtensions(String[] ext) {
        this.settings.extensions = ext;
    }

    /**
     * Sets file extension of files to ignore as input to template library.
     * 
     * @param ignore
     *     An Array of Strings denoting file extensions to ignore as input when parsing
     *     a Template library.
     */
    public void setIgnoreExtensions(String[] ignore) {
        this.settings.ignoreExtensions = ignore;
    }
    
    /**
     * Registers a new Format to this' FormatManager, and
     * will subsequently be used for parsing operations when no
     * Format is specified, together will all other registered Formats.
     * 
     * @param format
     *      The Format to register to this' FormatManager.
     * @see FormatManager#attemptAllFormats(Function)
     */
    public void registerFormat(Format format) {
        format.setPrefixMapping(this.prefixes);
        this.formatManager.register(format);
    }

    /**
     * Registers a collection of new Formats to this' FormatManager, and
     * will subsequently be used for parsing operations when no
     * Format is specified, together will all other registered Formats.
     * 
     * @param formats
     *      The Collection of Formats to register to this' FormatManager.
     * @see FormatManager#attemptAllFormats(Function)
     */
    public void registerFormats(Collection<Format> formats) {
        this.formatManager.register(formats);
    }

    /**
     * Retrieves Format by name from this' FormatManager.
     * 
     * @param formatName
     *      The name of the Format to retrieve.
     * @return
     *      The Format with the argument name.
     */
    public Format getFormat(String formatName) {
        return this.formatManager.getFormat(formatName);
    }
    
    /**
     * Creates a TemplateStore using the argument FormatManager and
     * containing the standard OTTR base templates.
     * 
     * @param formatManager
     *      The FormatManager which the TemplateStore should use.
     * @return
     *      A TemplateStore containing the OTTR base templates.
     * @see OTTR.BaseTemplate
     */
    public static TemplateStore makeDefaultStore(FormatManager formatManager) {
        TemplateStore store = new StandardTemplateStore(formatManager);
        store.addOTTRBaseTemplates();
        return store;
    }

    /**
     * Gets this' TemplateStore used for all Template-related operations..
     * 
     * @return
     *      This' TemplateStore used for all Template-related operations..
     */
    public TemplateStore getTemplateStore() {
        return this.templateStore;
    }
    
    /**
     * Gets this' PrefixMapping used when writing/serializing Instances and Templates.
     * 
     * @return
     *      This' PrefixMapping used when writing/serializing Instances and Templates.
     */
    public PrefixMapping getPrefixes() {
        return this.prefixes;
    }

    /**
     * Adds all the prefix mappings in the argument PrefixMaping to this's PrefixMapping.
     * 
     * @param other
     *      PrefixMapping containing the prefix mapping to add.
     */
    public void addPrefixes(PrefixMapping other) {
        this.prefixes.setNsPrefixes(other);
    }

    /**
     * Populates this' TemplateStore with parsed Templates from the argument folders,
     * and returns a MessageHandler with potential errors that occurred during parsing.
     * As no Format is provided, it will attempt all Formats registered and use the
     * first that succeeds.
     * 
     * @param library
     *      Strings denoting paths to folders which should be parsed and loaded into this'
     *      TemplateStore.
     * @return
     *      A MessageHandler containing all Messages generated during the parsing.
     */
    public MessageHandler readLibrary(String... library) {
        return readLibrary(null, library);
    }

    /**
     * Populates this' TemplateStore with parsed Templates from the argument folders,
     * and returns a MessageHandler with potential errors that occurred during parsing.
     * 
     * @param format
     *      Format to use for the parsing. 
     * @param library
     *      Strings denoting paths to folders which should be parsed and loaded into this'
     *      TemplateStore.
     * @return
     *      A MessageHandler containing all Messages generated during the parsing.
     */
    public MessageHandler readLibrary(Format format, String... library) {
        return readLibrary(format, Arrays.asList(library));
    }

    /**
     * Populates this' TemplateStore with parsed Templates from the argument folders,
     * and returns a MessageHandler with potential errors that occurred during parsing.
     * 
     * @param format
     *      Format to use for the parsing. 
     * @param library
     *      A Collection of Strings denoting paths to folders which should be parsed and
     *      loaded into this' TemplateStore.
     * @return
     *      A MessageHandler containing all Messages generated during the parsing.
     */
    public MessageHandler readLibrary(Format format, Collection<String> library) {
        
        MessageHandler messages = new MessageHandler();

        for (String lib : library) {
            // check if library is folder or file, and get readerFunction accordingly:

            Function<TemplateReader, MessageHandler> readerFunction =
                Files.isDirectory(Paths.get(lib))
                    ? reader -> reader.loadTemplatesFromFolder(this.templateStore, lib,
                    this.settings.extensions, this.settings.ignoreExtensions)
                    : reader -> reader.loadTemplatesFromFile(this.templateStore, lib);

            Result<TemplateReader> reader;
            // check if libraryFormat is set or not
            if (format != null) {
                reader = format.getTemplateReader();
                reader.map(readerFunction).map(messages::combine);
            } else {
                reader = this.templateStore.getFormatManager().attemptAllFormats(readerFunction);
            }
            messages.add(reader);
            reader.ifPresent(r -> this.prefixes.setNsPrefixes(r.getPrefixes()));
        }
        
        messages.combine(fetchMissingDependencies());

        return messages;
    } 
    
    public MessageHandler fetchMissingDependencies() {

        MessageHandler messages = new MessageHandler();
        if (this.settings.fetchMissingDependencies) {
            messages.combine(this.templateStore.fetchMissingDependencies());
        }
        return messages;
    }
        

    /**
     * Reads the instances contained in files denoted by argument Strings,
     * using the argument Format.
     * 
     * @param format
     *      The format to use for parsing the instances.
     * @param files
     *      Strings denoting paths to files containing instances.
     * @return
     *      A ResultStream of Instances, where the contained results
     *      also contains possible Messages generated during parsing.
     */
    public ResultStream<Instance> readInstances(Format format, String... files) {
        return readInstances(format, Arrays.asList(files));
    }

    /**
     * Reads the instances contained in files denoted by the Strings in the argument
     * Collection, using the argument Format.
     * 
     * @param format
     *      The format to use for parsing the instances.
     * @param files
     *      Collection of strings denoting paths to files containing instances.
     * @return
     *      A ResultStream of Instances, where the contained results
     *      also contains possible Messages generated during parsing.
     */
    public ResultStream<Instance> readInstances(Format format, Collection<String> files) {

        Result<InstanceReader> reader = format.getInstanceReader();
        ResultStream<String> fileStream = ResultStream.innerOf(files);

        return reader.mapToStream(fileStream::innerFlatMap);
    }
    
    /**
     * Makes an Instance expander based on this' settings and TemplateStore.
     * 
     * @return
     *      A Function that maps Instances to a ResultStream containing
     *      the arguments expansion according to the Templates in
     *      this' TemplateStore.
     */
    public Function<Instance, ResultStream<Instance>> makeExpander() {
        Expander expander = new CheckingExpander(templateStore);
        if (this.settings.fetchMissingDependencies) {
            return expander::expandInstanceFetch;
        } else {
            return expander::expandInstance;
        }
    }
    
    /**
     * Creates a new StandardTemplateStore equivalent to this, but with this' TemplateStore expanded.
     * 
     * @return
     *      A Result containing an equivalent StandardTemplateStore to this, except that
     *      with an expanded TemplateStore. This Result might be empty and contain
     *      errors or other Messages if something went wrong during expansion (e.g.
     *      missing definitions).
     */
    public Result<TemplateManager> expandStore() {
        Expander expander = new CheckingExpander(templateStore);
        return expander.expandAll().map(expanded ->
            new TemplateManager(this.settings, this.prefixes, this.formatManager, expanded)
        );
    }
    
    /**
     * Checks all Template objects in this' TemplateStore.
     * 
     * @return
     *      A MessageHandler containing potential warnings and errors
     *      generated from the checks.
     * @see TemplateStore#checkTemplates()
     */
    public MessageHandler checkTemplates() {
        return this.templateStore.checkTemplates();
    }
    
    /**
     * Writes the each of the Instances in the argument ResultStream to file.
     * All Messages produced by these function applications is then gathered
     * into the returned MessageHandler.
     *      *
     * @param instances
     *      A ResultStream of Instances to write to String.
     * @param format
     *      The Format used to write the Instances to String.
     * @param filePath
     *      A String containing the file path for out file
     * @param consoleStream
     *      A PrintStream for console output
     * @return
     *      A MessageHandler containing all Messages generated by the function
     *      applications.
     */
    public MessageHandler writeInstances(ResultStream<Instance> instances, Format format, String filePath, PrintStream consoleStream) {
        Result<InstanceWriter> writerRes = format.getInstanceWriter();
        MessageHandler msgs = writerRes.getMessageHandler();
        if (!writerRes.isPresent()) {
            return msgs;
        }
        msgs.combine(writerRes.get().init(filePath, consoleStream));
        msgs.combine(writeObjects(instances, writerRes));
        msgs.combine(writerRes.get().flush());
        msgs.combine(writerRes.get().close());
        return msgs;
    }

    /**
     * Writes the each of the Template-objects in this' TemplateStore to String
     * using argument Format, which, together with the Template-object's IRI,
     * is then given to argument Function (IRI as first argument, written String as
     * second argument). All Messages produced by these function applications is
     * then gathered into the returned MessageHandler.
     * 
     * The argument Function could e.g. write the Strings to a file
     * or to some other output.  
     * 
     * @param format
     *      The Format used to write the Template-objects to String.
     * @param stringConsumer
     *      A Function to which the written Strings will be applied.
     * @return
     *      A MessageHandler containing all Messages generated by the function
     *      applications.
     */
    public MessageHandler writeTemplates(Format format, BiFunction<String, String, Optional<Message>> stringConsumer) {

        Result<TemplateWriter> writerRes = format.getTemplateWriter();

        return writeObjects(this.templateStore.getAllSignatures(), writerRes, (writer, msgs) -> {
            for (String iri : writer.getIRIs()) {
                stringConsumer.apply(iri, writer.write(iri)).ifPresent(msgs::add);
            }
        });
        /*MessageHandler msgs = writerRes.getMessageHandler();
        if (!writerRes.isPresent()) {
            return msgs;
        }
        writerRes.get().setWriterFunction(stringConsumer);
        msgs.combine(writeObjects(this.templateStore.getAllTemplateObjects(), writerRes));
        msgs.combine(writerRes.get().getMessages());
        return msgs;*/
    }

    private <T, W extends Consumer<T>> MessageHandler writeObjects(ResultStream<T> objects,
            Result<W> writerRes) {

        MessageHandler msgs = writerRes.getMessageHandler();

        writerRes.ifPresent(writer -> {
            ResultConsumer<T> consumer = new ResultConsumer<>(writer);
            objects.forEach(consumer);
            msgs.combine(consumer.getMessageHandler());
        });

        return msgs;
    }

    static class Settings {

        public boolean deepTrace;
        public boolean stackTrace;
        public boolean fetchMissingDependencies;
        //public Message.Severity haltOn = Message.Severity.ERROR;

        public String[] extensions = { };
        public String[] ignoreExtensions = { };

        //public FormatName fetchFormat; // TODO: Find how to use
    }
}
