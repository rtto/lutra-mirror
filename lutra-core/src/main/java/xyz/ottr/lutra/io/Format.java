package xyz.ottr.lutra.io;

import org.apache.jena.shared.PrefixMapping;

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

import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.writer.InstanceWriter;
import xyz.ottr.lutra.writer.TemplateWriter;

public interface Format {
    
    enum Operation { read, write }

    enum ObjectType { template, instance }
    
    /**
     * Gets a Result containing this Format's TemplateReader, if reading templates
     * is supported, and an empty Result with an error Message otherwise.
     */
    default Result<TemplateReader> getTemplateReader() {
        return Result.error("Reading templates not supported for format " + getFormatName());
    }

    /**
     * Gets a Result containing this Format's TemplateWriter, if writing templates
     * is supported, and an empty Result with an error Message otherwise.
     */
    default Result<TemplateWriter> getTemplateWriter() {
        return Result.error("Writing templates not supported for format " + getFormatName());
    }

    /**
     * Gets a Result containing this Format's InstanceReader, if reading instances
     * is supported, and an empty Result with an error Message otherwise.
     */
    default Result<InstanceReader> getInstanceReader() {
        return Result.error("Reading instances not supported for format " + getFormatName());
    }

    /**
     * Gets a Result containing this Format's InstanceWriter, if writing instances
     * is supported, and an empty Result with an error Message otherwise.
     */
    default Result<InstanceWriter> getInstanceWriter() {
        return Result.error("Writing instances not supported for format " + getFormatName());
    }
    
    /**
     * Returns true if the operation described by the argument enums is supported.
     * Used as default implementation for all of the other supportsXXX-methods.
     * 
     * @param op
     *      Either of the members of Format.Operation
     * @param ot
     *      Either of the members of Format.ObjectType
     * @return
     *      True if the operation described by the argument enums is supported,
     *      false otherwise.
     */
    boolean supports(Operation op, ObjectType ot);
    
    /**
     * Gets the default file suffix for this Format (e.g. ".ttl" for RDF-files).
     */
    String getDefaultFileSuffix();
    
    /**
     * Gets the name of this Format. Used in Messages and for lookup.
     */
    String getFormatName();
    
    /**
     * @see #supports(Operation, ObjectType)
     */
    default boolean supportsTemplateWriter() {
        return supports(Operation.write, ObjectType.template);
    }

    /**
     * @see #supports(Operation, ObjectType)
     */
    default boolean supportsTemplateReader() {
        return supports(Operation.read, ObjectType.template);
    }

    /**
     * @see #supports(Operation, ObjectType)
     */
    default boolean supportsInstanceWriter() {
        return supports(Operation.write, ObjectType.instance);
    }

    /**
     * @see #supports(Operation, ObjectType)
     */
    default boolean supportsInstanceReader() {
        return supports(Operation.read, ObjectType.instance);
    }

    /**
     * Sets the prefixes used for writing operations. If
     * this Format does not support any writing operations
     * this method should do nothing.
     * 
     * OBS: The TemplateManager assumes that the Format keeps
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
