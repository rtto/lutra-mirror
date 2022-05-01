package xyz.ottr.lutra.io;

import java.util.Collection;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.writer.InstanceWriter;
import xyz.ottr.lutra.writer.TemplateWriter;

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

/**
 * An implementation of <code>Format</code> corresponds to one OTTR format 
 * (wOTTR, bOTTR, stOTTR, etc.) and can be used to check what operations the 
 * format supports (reading templates, writing instances, etc.), and contains 
 * convenience methods for creating readers ({@link TemplateReader}, 
 * {@link InstanceReader}) and writers ({@link xyz.ottr.lutra.writer.TemplateWriter}, 
 * {@link xyz.ottr.lutra.writer.InstanceWriter}) for the format.
 * A <code>Format</code> is typically retrieved from a {@link FormatManager}.
 */
public interface Format {

    enum Support {
        InstanceReader,
        InstanceWriter,

        TemplateReader,
        TemplateWriter
    }

    default String errorMessage(String operation) {
        return "Unsupported format operation. The format " + getFormatName() + " does not support " + operation + ".";
    }

    /**
     * Gets a Result containing this Format's TemplateReader, if reading templates
     * is supported, and an empty Result with an error Message otherwise.
     */
    default Result<TemplateReader> getTemplateReader() {
        return Result.error(errorMessage("reading templates"));
    }

    /**
     * Gets a Result containing this Format's TemplateWriter, if writing templates
     * is supported, and an empty Result with an error Message otherwise.
     */
    default Result<TemplateWriter> getTemplateWriter() {
        return Result.error(errorMessage("writing templates"));
    }

    /**
     * Gets a Result containing this Format's InstanceReader, if reading instances
     * is supported, and an empty Result with an error Message otherwise.
     */
    default Result<InstanceReader> getInstanceReader() {
        return Result.error(errorMessage("reading instances"));
    }

    /**
     * Gets a Result containing this Format's InstanceWriter, if writing instances
     * is supported, and an empty Result with an error Message otherwise.
     */
    default Result<InstanceWriter> getInstanceWriter() {
        return Result.error(errorMessage("writing instances"));
    }

    Collection<Support> getSupport();

    /**
     * Gets the default file suffix for this Format (e.g. ".ttl" for RDF-files).
     */
    default String getDefaultFileSuffix() {
        throw new UnsupportedOperationException(errorMessage("write operations"));
    }
    
    /**
     * Gets the name of this Format. Used in Messages and for lookup.
     */
    String getFormatName();

    default boolean supportsTemplateWriter() {
        return getSupport().contains(Support.TemplateWriter);
    }

    default boolean supportsTemplateReader() {
        return getSupport().contains(Support.TemplateReader);
    }

    default boolean supportsInstanceWriter() {
        return getSupport().contains(Support.InstanceWriter);
    }

    default boolean supportsInstanceReader() {
        return getSupport().contains(Support.InstanceReader);
    }

    /**
     * Sets the prefixes used for writing operations. If
     * this Format does not support any writing operations
     * this method should do nothing.
     * 
     * OBS: The StandardTemplateStore assumes that the Format keeps
     * the reference to the argument, and does not make a copy
     * of the PrefixMapping, as more prefixes might be added to
     * the map at a later stage.
     * 
     * @param prefixes
     *      A PrefixMapping containing prefixes that should be used
     *      for writing operations.
     */
    void setPrefixMapping(PrefixMapping prefixes);
}
