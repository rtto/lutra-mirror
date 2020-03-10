package xyz.ottr.lutra.io;

/*-
 * #%L
 * lutra-core
 * %%
 * Copyright (C) 2018 - 2019 University of Oslo
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

import java.util.Map;
import java.util.function.Function;

import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.system.Result;

public interface ReaderRegistry {

    default Result<TemplateReader> getTemplateReaders(String format) {
        return Result.ofNullable(getAllTemplateReaders().get(format));
    }

    default Result<InstanceReader> getInstanceReader(String format) {
        return Result.ofNullable(getAllInstanceReaders().get(format));
    }

    void registerTemplateReader(TemplateReader reader);

    void registerInstanceReader(InstanceReader reader);

    Map<String, TemplateReader> getAllTemplateReaders();

    Map<String, InstanceReader> getAllInstanceReaders();
    
    Result<TemplateReader> attemptAllReaders(Function<TemplateReader, MessageHandler> todo);
}
