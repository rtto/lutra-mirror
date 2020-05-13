package xyz.ottr.lutra.io;

import java.util.Collection;

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

    enum Support {
        InstanceReader,
        InstanceWriter,

        TemplateReader,
        TemplateWriter
    }

    default String errorMessage(String operation) {
        return "Unsupported format operation. The format " + getFormatName() + " does not support " + operation + ".";
    }

    default Result<TemplateReader> getTemplateReader() {
        return Result.error(errorMessage("reading templates"));
    }

    default Result<TemplateWriter> getTemplateWriter() {
        return Result.error(errorMessage("writing templates"));
    }

    default Result<InstanceReader> getInstanceReader() {
        return Result.error(errorMessage("reading instances"));
    }

    default Result<InstanceWriter> getInstanceWriter() {
        return Result.error(errorMessage("writing instances"));
    }

    Collection<Support> getSupport();

    default String getDefaultFileSuffix() {
        throw new UnsupportedOperationException(errorMessage("write operations"));
    }
    
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

    void setPrefixMapping(PrefixMapping prefixes);
}
