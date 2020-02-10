package xyz.ottr.lutra.io;

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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.MessageHandler;
import xyz.ottr.lutra.result.Result;

public class FormatManager {

    public static final Set<FormatName> allowedInstanceReaderFormats = Collections.unmodifiableSet(Set.of(
            FormatName.legacy, FormatName.wottr, FormatName.stottr, FormatName.tabottr, FormatName.bottr));
    public static final Set<FormatName> allowedInstanceWriterFormats = Collections.unmodifiableSet(Set.of(
            FormatName.wottr, FormatName.stottr));
    public static final Set<FormatName> allowedTemplateReaderFormats = Collections.unmodifiableSet(Set.of(
            FormatName.legacy, FormatName.wottr, FormatName.stottr));
    public static final Set<FormatName> allowedTemplateWriterFormats = Collections.unmodifiableSet(Set.of(
            FormatName.wottr, FormatName.stottr));

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

    private static <T> Optional<Message> registerFormatTo(FormatName name, T format, Map<FormatName, T> formats,
            Collection<FormatName> allowed, String operation) {

        if (!allowed.contains(name)) {
            return Optional.of(Message.error("Format " + name + " not allowed as " + operation + " format."));
        }

        if (formats.containsKey(name)) {
            return Optional.of(Message.error("Format " + name + " already added to this FormatManager as " + operation + "."));
        }

        formats.put(name, format);
        return Optional.empty();
    }


    public Optional<Message> registerInstanceReader(InstanceReader format) {
        return registerFormatTo(format.getFormat(), format, this.instanceReaders, allowedInstanceReaderFormats, "instance input");
    }

    public Optional<Message> registerInstanceWriter(InstanceWriter format) {
        return registerFormatTo(format.getFormat(), format, this.instanceWriters, allowedInstanceWriterFormats, "instance output");
    }

    public Optional<Message> registerTemplateReader(TemplateReader format) {
        return registerFormatTo(format.getFormat(), format, this.templateReaders, allowedTemplateReaderFormats, "template input");
    }

    public Optional<Message> registerTemplateWriter(TemplateWriter format) {
        return registerFormatTo(format.getFormat(), format, this.templateWriters, allowedTemplateWriterFormats, "template output");
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

    public Result<InstanceReader> getInstanceReader(FormatName name) {
        return Result.ofNullable(this.instanceReaders.get(name));
    }

    public Result<InstanceWriter> getInstanceWriter(FormatName name) {
        return Result.ofNullable(this.instanceWriters.get(name));
    }

    public Result<TemplateReader> getTemplateReader(FormatName name) {
        return Result.ofNullable(this.templateReaders.get(name));
    }

    public Result<TemplateWriter> getTemplateWriter(FormatName name) {
        return Result.ofNullable(this.templateWriters.get(name));
    }

    public Result<TemplateReader> attemptAllFormats(Function<TemplateReader, MessageHandler> readerFunction) {
        
        Result<TemplateReader> unsuccessful = Result.empty(); // Return in case of no succeed
        for (Map.Entry<FormatName, TemplateReader> reader : getTemplateReaders().entrySet()) {
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
            + getTemplateReaders().keySet().toString() + " failed with following errors:\n");

        return errors.isPresent() ? Result.empty(errors.get()) : Result.empty();
    }
}
