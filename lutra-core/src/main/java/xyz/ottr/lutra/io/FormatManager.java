package xyz.ottr.lutra.io;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.system.Result;

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

public class FormatManager {

    private final Map<String, Format> formats;

    private FormatManager(Map<String, Format> formats) {
        this.formats = formats;
    }

    public FormatManager() {
        this(new HashMap<>());
    }

    /**
     * Creates a new FormatManager with Formats in argument
     * Collection pre-registered.
     * 
     * @param formats
     *      A Collection of Formats to register.
     */
    public FormatManager(Collection<Format> formats) {
        this();
        register(formats);
    }

    private static String getKey(String formatName) {
        return formatName.toLowerCase(Locale.getDefault());
    }

    /**
     * Registers the argument Format so that it can be
     * retrieved based on name, and attempted used for parsing
     * operations where no Format is specified.
     * 
     * @param format
     *      Format to register.
     */
    public void register(Format format) {

        String key = getKey(format.getFormatName());

        if (this.formats.containsKey(key) && !this.formats.get(key).equals(format)) {
            throw new IllegalArgumentException("Format named " + format.getFormatName()
                + " already registered in FormatManager to a different Format.");
        }

        this.formats.put(getKey(format.getFormatName()), format);
    }

    /**
     * Registers the Formats in the argument Collection so that they can be
     * retrieved based on name, and attempted used for parsing
     * operations where no Format is specified.
     * 
     * @param format
     *      Collection of Formats to register.
     */
    public void register(Collection<Format> formats) {
        formats.forEach(this::register);
    }

    /**
     * Gets the registered Format with the argument name.
     * 
     * @param formatName
     *      Name of Format to retrieve.
     * @return
     *      The registered Format with the given name, or null if
     *      no Format with that name is registered. 
     */
    public Format getFormat(String formatName) {
        return this.formats.get(getKey(formatName));
    }

    /**
     * Gets all Formats registered.
     * 
     * @return
     *      A Collection of all registered Formats.
     */
    public Collection<Format> getFormats() {
        return Collections.unmodifiableCollection(this.formats.values());
    }

    /**
     * Attempts to apply argument reader function to registered Format's 
     * TemplateReader in turn for those Formats supporting TemplateReader. The argument
     * Function should return a MessageHandler containing at least one Message
     * of severity {@link Message#ERROR} or higher if the attempt failed
     * (this is default behavior for all TemplateReaders).
     * When a Format succeeds (i.e. a MessageHandler with no errors is returned
     * from the function application), the method returns with the TemplateReader
     * that succeeded wrapped in a Result. If no Format succeeded, an empty
     * Result is returned with an error Message describing what failed for each Format.
     * 
     * @param readerFunction
     *      A Function that takes a TemplateReader and attempts to use it in some computation,
     *      and returns a MessageHandler with a Message of severity {@link Message#ERROR}
     *      or higher if the attempt failed.
     * @return
     *      A Result either containing the TemplateReader that succeeded, or an empty
     *      Result with an error Message describing what went wrong for each reader
     *      if no Format's TemplateReader succeeded.
     */
    public Result<TemplateReader> attemptAllFormats(Function<TemplateReader, MessageHandler> readerFunction) {
        
        Result<TemplateReader> unsuccessful = Result.empty(); // Return in case of no succeed
        for (Format format : this.formats.values()) {
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
