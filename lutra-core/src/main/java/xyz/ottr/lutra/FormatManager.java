package xyz.ottr.lutra;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.io.InstanceWriter;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.io.TemplateWriter;

public class FormatManager {

    public static final Set<FormatName> allowedInstanceReaderFormats = Collections.unmodifiableSet(legacy, wottr, stottr, tabottr, bottr);
    public static final Set<FormatName> allowedInstanceWriterFormats = Collections.unmodifiableSet(wottr, stottr);
    public static final Set<FormatName> allowedTemplateReaderFormats = Collections.unmodifiableSet(legacy, wottr, stottr);
    public static final Set<FormatName> allowedTemplateWriterFormats = Collections.unmodifiableSet(wottr, stottr);

    private final Map<FormatName, InstanceReader> instanceReaders;
    private final Map<FormatName, InstanceWriter> instanceWriters;
    private final Map<FormatName, TemplateReader> templateReaders;
    private final Map<FormatName, TemplateWriter> templateWriters;

    public FormatManager() {
        this.instanceReaders = new HashMap<>();
        this.instanceWriters = new HashMap<>();
        this.templateReaders = new HashMap<>();
        this.templateWriters = new HashMap<>();
    }

    private static Optional<Message> registerFormatTo(FormatName name, T format, Map<FormatName, T> formats, Collection<FormatName> allowed, String operation) {

        if (!allowed.contains(name)) {
            return Optional.of(Message.error("Format " + name + " not allowed as " + operation + " format."));
        }

        if (formats.containsKey(name)) {
            return Optional.of(Message.error("Format " + name + " already added to this FormatManager as " + operation + "."));
        }

        formats.put(name, format);
        return Optional.empty();
    }


    public Optional<Message> registerInstanceReader(FormatName name, InstanceReader format) {
        return registerFormatTo(name, format, this.instanceReaders, allowedInstanceReaderFormats, "instance input");
    }

    public Optional<Message> registerInstanceWriter(FormatName name, InstanceWriter format) {
        return registerFormatTo(name, format, this.instanceWriters, allowedInstanceWriterFormats, "instance output");
    }

    public Optional<Message> registerTemplateReader(FormatName name, TemplateReader format) {
        return registerFormatTo(name, format, this.templateReaders, allowedTemplateReaderFormats, "template input");
    }

    public Optional<Message> registerTemplateWriter(FormatName name, TemplateWriter format) {
        return registerFormatTo(name, format, this.templateWriters, allowedTemplateWriterFormats, "template output");
    }

    public Map<FormatName, InstanceReader> getInstanceReaders() {
        return Collections.unmodifiableMap(this.instanceReaders);
    }

    public Map<FormatName, InstanceWriter> getInstanceWriters() {
        return Collections.unmodifiableMap(this.instanceWriters);
    }

    public Map<FormatName, TemplateReader> getTemplateReaders() {
        return Collections.unmodifiableMap(this.templateReaders);
    }

    public Map<FormatName, TemplateWriter> getTemplateWriters() {
        return Collections.unmodifiableMap(this.templateWriters);
    }

    public InstanceReader getInstanceReader(FormatName name) {
        return this.instanceReaders.get(name);
    }

    public InstanceWriter getInstanceWriter(FormatName name) {
        return this.instanceWriters.get(name);
    }

    public TemplateReader getTemplateReader(FormatName name) {
        return this.templateReaders.get(name);
    }

    public TemplateWriter getTemplateWriter(FormatName name) {
        return this.templateWriters.get(name);
    }

    private Result<Format> attemptAllFormats(Function<Format, MessageHandler> function, Map<FormatName, Format> formats) {
        
        Result<Format> unsuccessful = Result.empty(); // Return in case of no succeed
        for (Map.Entry<String, Format> format : getAllTemplateReaders().entrySet()) {
            MessageHandler msgs = readerFunction.apply(reader.getValue());

            if (Message.moreSevere(msgs.getMostSevere(), Message.ERROR)) {
                msgs.toSingleMessage("Attempt of parsing templates as "
                    + reader.getKey() + " format failed:")
                    .ifPresent(unsuccessful::addMessage);
            } else {
                Result<TemplateReader> readerRes = Result.of(reader.getValue());
                msgs.toSingleMessage("")
                    .ifPresent(readerRes::addMessage);
                return readerRes;
            }
        }

        // Combine all errors in the failed attempts into one message
        MessageHandler allMsgs = new MessageHandler();
        allMsgs.add(unsuccessful);
        Optional<Message> errors = allMsgs.toSingleMessage(
            "Attempts of parsing library on all available formats " 
            + getAllTemplateReaders().keySet().toString() + " failed with following errors:\n");

        return errors.isPresent() ? Result.empty(errors.get()) : Result.empty();
    }
}
