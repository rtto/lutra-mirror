package xyz.ottr.lutra.io;

import java.util.Collection;
import java.util.Collections;

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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.MessageHandler;
import xyz.ottr.lutra.result.Result;

public class FormatManager {

    private final Set<Format> formats;

    public FormatManager(Collection<Format> formats) {
        this.formats = new HashSet<>(formats);
    }

    public FormatManager() {
        this(new HashSet<>());
    }

    public void register(Format format) {
        this.formats.add(format);
    }

    public void register(Collection<Format> formats) {
        this.formats.addAll(formats);
    }
    
    public Set<Format> getFormats() {
        return Collections.unmodifiableSet(this.formats);
    }

    public Result<TemplateReader> attemptAllFormats(Function<TemplateReader, MessageHandler> readerFunction) {
        
        Result<TemplateReader> unsuccessful = Result.empty(); // Return in case of no succeed
        for (Format format : this.formats) {
            if (!format.supportsTemplateReader()) {
                continue;
            }
            TemplateReader reader = format.getTemplateReader().get();
            MessageHandler msgs = readerFunction.apply(reader);

            if (Message.moreSevere(msgs.getMostSevere(), Message.ERROR)) {
                msgs.toSingleMessage("Attempt of parsing templates as "
                    + reader.toString() + " format failed:")
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
            + this.formats.toString() + " failed with following errors:\n");

        return errors.isPresent() ? Result.empty(errors.get()) : Result.empty();
    }
}
