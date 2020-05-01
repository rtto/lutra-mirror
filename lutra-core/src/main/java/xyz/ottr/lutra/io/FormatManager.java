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
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.system.Result;

public class FormatManager {

    private final Map<String, Format> formats;

    public FormatManager(Map<String, Format> formats) {
        this.formats = formats;
    }

    public FormatManager() {
        this(new LinkedHashMap<>());
    }

    public FormatManager(Collection<Format> formats) {
        this();
        register(formats);
    }

    private static String getKey(String formatName) {
        return formatName.toLowerCase(Locale.getDefault());
    }

    public void register(Format format) {

        String key = getKey(format.getFormatName());

        if (this.formats.containsKey(key)) {
            throw new IllegalArgumentException("Format named " + format.getFormatName() + " already registered in FormatManager.");
        }

        this.formats.put(getKey(format.getFormatName()), format);
    }

    public void register(Collection<Format> formats) {
        formats.forEach(this::register);
    }

    public Format getFormat(String formatName) {
        return this.formats.get(getKey(formatName));
    }

    public Collection<Format> getFormats() {
        return Collections.unmodifiableCollection(this.formats.values());
    }

    public Result<TemplateReader> attemptAllFormats(Function<TemplateReader, MessageHandler> readerFunction) {
        
        Result<TemplateReader> unsuccessful = Result.empty(); // Return in case of no succeed
        for (Format format : this.formats.values()) {
            if (!format.supportsTemplateReader()) {
                continue;
            }
            TemplateReader reader = format.getTemplateReader().get();
            MessageHandler msgs = readerFunction.apply(reader);

            if (msgs.getMostSevere().isGreaterEqualThan(Message.Severity.ERROR)) {
                msgs.toSingleMessage("Attempt of parsing templates as " + reader + " format failed:")
                    .ifPresent(unsuccessful::addMessage);
            } else {
                Result<TemplateReader> readerRes = Result.of(reader);
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
            + this.formats + " failed with following errors:\n");

        return errors.isPresent()
            ? Result.empty(errors.get())
            : Result.empty();
    }

}
