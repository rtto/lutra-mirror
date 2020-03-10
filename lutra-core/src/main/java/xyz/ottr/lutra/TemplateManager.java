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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.jena.shared.PrefixMapping;

import xyz.ottr.lutra.io.Format;
import xyz.ottr.lutra.io.FormatManager;
import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.io.InstanceWriter;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.io.TemplateWriter;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.MessageHandler;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultConsumer;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.result.Trace;
import xyz.ottr.lutra.store.DependencyGraph;
import xyz.ottr.lutra.store.TemplateStore;

public class TemplateManager {

    private final Settings settings;
    private final PrefixMapping prefixes;
    private final FormatManager formatManager;
    private final TemplateStore store;
    
    private TemplateManager(Settings settings, PrefixMapping prefixes, FormatManager formatManager, TemplateStore store) {
        this.store = store;
        this.formatManager = formatManager;
        this.settings = settings;
        this.prefixes = prefixes;
        Trace.setDeepTrace(this.settings.deepTrace);
    }

    private TemplateManager(FormatManager formatManager, TemplateStore store) {
        this(new Settings(), PrefixMapping.Factory.create().setNsPrefixes(OTTR.getDefaultPrefixes()),
            formatManager, store);
    }
    
    public TemplateManager(TemplateStore store) {
        this(store.getFormatManager(), store);
    }
    
    public TemplateManager(FormatManager formatManager) {
        this(formatManager, makeDefaultStore(formatManager));
    }
    
    public TemplateManager() {
        this(new FormatManager());
    }
    
    public void setDeepTrace(boolean enable) {
        this.settings.deepTrace = enable;
        Trace.setDeepTrace(this.settings.deepTrace);
    }
    
    public void setFetchMissingDependencies(boolean enable) {
        this.settings.fetchMissingDependencies = enable;
    }

    public void setHaltOn(int lvl) {
        this.settings.haltOn = lvl;
    }

    public void setExtensions(String[] ext) {
        this.settings.extensions = ext;
    }

    public void setIgnoreExtensions(String[] ignore) {
        this.settings.ignoreExtensions = ignore;
    }
    
    public void registerFormat(Format format) {
        format.setPrefixMapping(this.prefixes);
        this.formatManager.register(format);
    }

    public void registerFormats(Collection<Format> formats) {
        this.formatManager.register(formats);
    }

    public Format getFormat(String formatName) {
        return this.formatManager.getFormat(formatName);
    }

    public static TemplateStore makeDefaultStore(FormatManager formatManager) {
        TemplateStore store = new DependencyGraph(formatManager);
        store.addOTTRBaseTemplates();
        return store;
    }

    public TemplateStore getTemplateStore() {
        return this.store;
    }
    
    public PrefixMapping getPrefixes() {
        return this.prefixes;
    }

    public void addPrefixes(PrefixMapping other) {
        this.prefixes.setNsPrefixes(other);
    }

    /**
     * Populated store with parsed templates, and returns messages with potential errors
     */
    // TODO rename to readLibrary?
    public MessageHandler parseLibraryInto(String... library) {
        return parseLibraryInto(null, library);
    }

    /**
     * Populated store with parsed templates, and returns messages with potential errors
     */
    public MessageHandler parseLibraryInto(Format format, String... library) {
        return parseLibraryInto(format, Arrays.asList(library));
    }

    /**
     * Populated store with parsed templates, and returns messages with potential errors
     */
    public MessageHandler parseLibraryInto(Format format, Collection<String> library) {
        
        MessageHandler messages = new MessageHandler();

        for (String lib : library) {
            // check if library is folder or file, and get readerFunction accordingly:

            Function<TemplateReader, MessageHandler> readerFunction =
                Files.isDirectory(Paths.get(lib))
                    ? reader -> reader.loadTemplatesFromFolder(this.store, lib,
                    this.settings.extensions, this.settings.ignoreExtensions)
                    : reader -> reader.loadTemplatesFromFile(this.store, lib);

            Result<TemplateReader> reader;
            // check if libraryFormat is set or not
            if (format != null) {
                reader = format.getTemplateReader();
                reader.map(readerFunction).map(messages::combine);
            } else {
                reader = this.store.getFormatManager().attemptAllFormats(readerFunction);
            }
            messages.add(reader);
            reader.ifPresent(r -> this.prefixes.setNsPrefixes(r.getPrefixes()));
        }

        if (this.settings.fetchMissingDependencies) {
            MessageHandler msgs = this.store.fetchMissingDependencies();
            messages.combine(msgs);
        }
        
        return messages;
    } 

    // TODO rename to readInstances?
    public ResultStream<Instance> parseInstances(Format format, String... files) {
        return parseInstances(format, Arrays.asList(files));
    }

    public ResultStream<Instance> parseInstances(Format format, Collection<String> files) {

        Result<InstanceReader> reader = format.getInstanceReader();
        ResultStream<String> fileStream = ResultStream.innerOf(files);

        return reader.mapToStream(fileStream::innerFlatMap);
    }
    
    public Function<Instance, ResultStream<Instance>> makeExpander() {
        if (this.settings.fetchMissingDependencies) {
            return this.store::expandInstanceFetch;
        } else {
            return this.store::expandInstance;
        }
    }
    
    public Result<TemplateManager> expandStore() {
        return this.store.expandAll().map(expanded ->
            new TemplateManager(this.settings, this.prefixes, this.formatManager, expanded)
        );
    }
    
    public MessageHandler checkTemplates() {
        return this.store.checkTemplates();
    }
    
    public MessageHandler writeInstances(ResultStream<Instance> instances, Format format,
                                         Function<String, Optional<Message>> stringConsumer) {

        Result<InstanceWriter> writerRes = format.getInstanceWriter();
        return writeObjects(instances, writerRes, (writer, msgs) ->
            stringConsumer.apply(writer.write()).ifPresent(msgs::add));
    }

    public MessageHandler writeTemplates(Format format,
                                         BiFunction<String, String, Optional<Message>> stringConsumer) {

        Result<TemplateWriter> writerRes = format.getTemplateWriter();

        return writeObjects(this.store.getAllTemplateObjects(), writerRes, (writer, msgs) -> {
            for (String iri : writer.getIRIs()) {
                stringConsumer.apply(iri, writer.write(iri)).ifPresent(msgs::add);
            }
        });
    }

    private <T, W extends Consumer<T>> MessageHandler writeObjects(ResultStream<T> objects,
            Result<W> writerRes, BiConsumer<W, MessageHandler> fileWriter) { 

        MessageHandler msgs = new MessageHandler();
        msgs.add(writerRes);

        writerRes.ifPresent(writer -> {
            ResultConsumer<T> consumer = new ResultConsumer<>(writer);
            objects.forEach(consumer);
            msgs.combine(consumer.getMessageHandler());

            if (!Message.moreSevere(msgs.getMostSevere(), this.settings.haltOn)) {
                fileWriter.accept(writer, msgs);
            }
        });
        return msgs;
    }



    static class Settings {

        public boolean deepTrace = false;
        public boolean fetchMissingDependencies = false;
        public int haltOn = Message.ERROR;

        public String[] extensions = { };
        public String[] ignoreExtensions = { };

        //public FormatName fetchFormat; // TODO: Find how to use
    }
}
