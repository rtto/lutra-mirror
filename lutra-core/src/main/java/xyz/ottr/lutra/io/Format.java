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

import xyz.ottr.lutra.result.Result;

public interface Format {
    
    enum Operation { read, write }

    enum ObjectType { template, instance }
    
    default Result<TemplateReader> getTemplateReader() {
        return Result.error("Reading templates not supported for format " + getFormatName());
    }

    default Result<TemplateWriter> getTemplateWriter() {
        return Result.error("Writing templates not supported for format " + getFormatName());
    }

    default Result<InstanceReader> getInstanceReader() {
        return Result.error("Reading instances not supported for format " + getFormatName());
    }

    default Result<InstanceWriter> getInstanceWriter() {
        return Result.error("Writing instances not supported for format " + getFormatName());
    }
    
    boolean supports(Operation op, ObjectType ot);
    
    String getDefaultFileSuffix();
    
    String getFormatName();
    
    default boolean supportsTemplateWriter() {
        return supports(Operation.write, ObjectType.template);
    }

    default boolean supportsTemplateReader() {
        return supports(Operation.read, ObjectType.template);
    }

    default boolean supportsInstanceWriter() {
        return supports(Operation.write, ObjectType.instance);
    }

    default boolean supportsInstanceReader() {
        return supports(Operation.read, ObjectType.instance);
    }
}
