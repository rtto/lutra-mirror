package xyz.ottr.lutra;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.shared.PrefixMapping;

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
import xyz.ottr.lutra.result.Trace;
import xyz.ottr.lutra.store.TemplateStore;

public class TemplateManager {

    private final Settings settings;
    private final PrefixMapping prefixes;
    private final FormatManager formatManager;

    public TemplateManager(FormatManager formatManager, Settings settings) {
        this.formatManager = formatManager;
        this.settings = settings;
        this.prefixes = PrefixMapping.Factory.create().setNsPrefixes(OTTR.getDefaultPrefixes());
        Trace.setDeepTrace(this.settings.deepTrace);
    }

    public TemplateStore makeDefaultStore() {
        TemplateStore store = new DependencyGraph(this.formatManager);
        store.addOTTRBaseTemplates();
        return store;
    }

    /**
     * Populated store with parsed templates, and returns messages with potensial errors
     */
    public MessageHandler parseLibraryInto(TemplateStore store, String.. library) {
        return parseLibraryInto(store, null, library);
    }

    /**
     * Populated store with parsed templates, and returns messages with potensial errors
     */
    public MessageHandler parseLibraryInto(TemplateStore store, FormatName format, String.. library) {
        
        MessageHandler messages = new MessageHandler();

        for (int i = 0; i < library.length; i++) {
            // check if library is folder or file, and get readerFunction accordingly:
            String lib = library[i];

            Function<TemplateReader, MessageHandler> readerFunction =
                Files.isDirectory(Paths.get(library[i]))
                    ? reader -> reader.loadTemplatesFromFolder(store, lib,
                    this.settings.extensions, this.settings.ignoreExtensions)
                    : reader -> reader.loadTemplatesFromFile(store, lib);

            Result<TemplateReader> reader;
            // check if libraryFormat is set or not
            if (format != null) {
                reader = store.getReaderRegistry().getTemplateReaders(format);
                reader.map(readerFunction).map(messages::combine);
            } else {
                reader = store.getReaderRegistry().attemptAllReaders(readerFunction);
            }
            messages.addResult(reader);
            reader.ifPresent(r -> this.prefixes.setNsPrefixes(r.getPrefixes()));
        }

        if (this.settings.fetchMissingDependencies) {
            MessageHandler msgs = store.fetchMissingDependencies();
            messageHandler.combine(msgs);
        }
        
        return messages;
    } 

    public ResultStream<Instance> parseInstances(FormatName format, String.. files) {

        Result<InstanceReader> reader = makeInstanceReader(format);
        ResultStream<String> fileStream = ResultStream.innerOf(Arrrays.toList(files));

        return reader.mapToStream(fileStream::innerFlatMap);
    }
    
    ////////////////////////////////////////////////////////////
    /// MAKER-METHODS                                        ///
    ////////////////////////////////////////////////////////////
            
    public Result<InstanceReader> makeInstanceReader(FormatName format) {
        return this.formatManager.getInstanceReader(format);
    }

    public Result<InstanceWriter> makeInstanceWriter(FormatName format) {
        return this.formatManager.getInstanceWriter(format);
    }

    public Result<TemplateReader> makeTemplateReader(FormatName format) {
        return this.formatManager.getTemplateReader(format);
    }

    public Result<TemplateWriter> makeTemplateWriter(FormatName format) {
        return this.formatManager.getTemplateWriter(format);
    }

    public Function<Instance, ResultStream<Instance>> makeExpander(TemplateStore store) {
        if (this.settings.fetchMissingDependencies) {
            return store::expandInstanceFetch;
        } else {
            return store::expandInstance;
        }
    }

    ////////////////////////////////////////////////////////////
    /// WRITER-METHODS, WRITING THINGS TO FILE               ///
    ////////////////////////////////////////////////////////////

    public MessageHandler writeInstances(ResultStream<Instance> instances, FormatName format, String out) {

        Result<InstanceWriter> writerRes = makeInstanceWriter(format);

        return writeObjects(instances, writerRes, (writer, msgs) -> writeInstances(writer.write(), out).ifPresent(msgs::add));
    }

    public MessageHandler writeTemplates(TemplateStore store, FormatName format, String folder) { 

        Result<TemplateWriter> writerRes = makeTemplateWriter(format);

        return writeObjects(store.getAllTemplateObjects(), writerRes, (writer, msgs) -> {
            for (String iri : writer.getIRIs()) {
                writeTemplate(iri, writer.write(iri), folder).ifPresent(msgs::add);
            }
        });
    }

    private <T, W implements Consumer<T>> MessageHandler writeObjects(ResultStream<T> objects,
            Result<W> writerRes, BiConsumer<W, MessageHandler> fileWriter) { 

        MessageHandler msgs = new MessageHandler();
        msgs.add(writerRes);

        witerRes.ifPresent(writer -> {
            ResultConsumer<T> consumer = new ResultConsumer<>(writer);
            objects.forEach(consumer);
            msgs.combine(consumer.getMessageHandler());

            if (!Message.moreSevere(msgs.getMostSevere(), this.settings.haltOn)) {
                fileWriter.accept(writer, msgs);
            }
        });
        return msgs;
    }
    //////
    // !!! Move all methods below to own class (e.g. Files.java) !!!
    //////
    
    private Optional<Message> writeInstances(String output, String filePath) {

        try {
            Files.write(Paths.get(filePath), output.getBytes(Charset.forName("UTF-8")));
        } catch (IOException ex) {
            Message err = Message.error("Error writing output: " + ex.getMessage());
            return Optional.of(err);
        }
        return Optional.empty();
    }

    private Optional<Message> writeTemplate(String iri, String output, String folder) {

        try {
            // TODO: cli-arg to decide extension
            String iriPath = iriToPath(iri);
            Files.createDirectories(Paths.get(folder, iriToDirectory(iriPath)));
            Files.write(Paths.get(folder, iriPath + getFileSuffix()), output.getBytes(Charset.forName("UTF-8")));
        } catch (IOException | URISyntaxException ex) {
            Message err = Message.error(
                "Error when writing output -- " + ex.getMessage());
            return Optional.of(err);
        }
        return Optional.empty();
    }

    private String getFileSuffix(FormatName format) {

        switch (format) {
            case legacy:
            case wottr:
                return ".ttl";
            case stottr:
                return ".stottr";
            default:
                return "";
        }
    }

    private static String iriToDirectory(String pathStr) {
        Path folder = Paths.get(pathStr).getParent();
        return folder == null ? null : folder.toString();
    }

    private static String iriToPath(String iriStr) throws URISyntaxException {
        return new URI(iriStr).getPath();
    }

    static class Settings {

        public boolean deepTrace = false;
        public boolean fetchMissingDependencies = false;
        public int haltOn = Message.ERROR;

        public String[] extensions = { };
        public String[] ignoreExtensions = { };

        public FormatName fetchFormat;
    }
}
