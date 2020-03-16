package xyz.ottr.lutra.api;

/*-
 * #%L
 * lutra-api
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;

import org.apache.commons.io.IOUtils;

import xyz.ottr.lutra.TemplateManager;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultConsumer;
import xyz.ottr.lutra.system.ResultStream;
import xyz.ottr.lutra.wottr.io.RDFInputStreamReader;
import xyz.ottr.lutra.wottr.parser.WTemplateParser;

@Getter
public final class StandardTemplateManager extends TemplateManager {
    
    private TemplateStore standardLibrary;
    
    public StandardTemplateManager() {
        this.loadFormats();
    }

    public MessageHandler loadStandardTemplateLibrary() {

        this.standardLibrary = makeDefaultStore(getFormatManager());
        var reader = ResultStream.innerFlatMapCompose(new RDFInputStreamReader(), new WTemplateParser());
        ResultConsumer<Signature> consumer = new ResultConsumer<>(this.standardLibrary);

        getLibraryFiles("", "templates-master")
            .innerMap(this::getResourceAsStream)
            .innerFlatMap(reader)
            .forEach(consumer);

        super.getTemplateStore().registerStandardLibrary(standardLibrary);

        return consumer.getMessageHandler();
    }
    
    private ResultStream<String> getLibraryFiles(String path, String subfolder) {

        String fullPath = path + subfolder + "/";
        var resFolder = getResourceAsStream(fullPath);
        
        if (subfolder.isEmpty() || resFolder == null) {
            return ResultStream.empty();
        } else if (fullPath.endsWith(".ttl")) {
            return ResultStream.innerOf(fullPath);
        }

        List<String> lines = new LinkedList<>();
        try {
            lines.addAll(IOUtils.readLines(resFolder, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            return ResultStream.of(Result.error(ex.getMessage()));
        }
        return ResultStream.innerOf(lines)
                .innerFlatMap(file -> getLibraryFiles(fullPath, file));
    }
    
    private void loadFormats() {
        for (StandardFormat format : StandardFormat.values()) {
            this.registerFormat(format.format);
        }
    }

    private InputStream getResourceAsStream(String resource) {
        final InputStream in = getContextClassLoader().getResourceAsStream(resource);

        return in == null ? getClass().getResourceAsStream(resource) : in;
    }

    private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public TemplateStore getStandardLibrary() {
        return this.standardLibrary;
    }
}
