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
import xyz.ottr.lutra.wottr.io.RDFIO;
import xyz.ottr.lutra.wottr.parser.WTemplateParser;

@Getter
public final class StandardTemplateManager extends TemplateManager {

    private static final String standardLibFolder = "tpl-library";
    private static final String templatesListFile = "templates-list.txt";


    public StandardTemplateManager() {
        this.loadFormats();
    }

    public MessageHandler loadStandardTemplateLibrary() {

        var standardLibrary = makeDefaultStore(getFormatManager());
        ResultConsumer<Signature> consumer = new ResultConsumer<>(standardLibrary);
        var reader = ResultStream.innerFlatMapCompose(RDFIO.inputStreamReader(), new WTemplateParser());

        getLibraryPaths()
            .innerMap(this::getResourceAsStream)
            .innerFlatMap(reader)
            .forEach(consumer);

        super.getTemplateStore().registerStandardLibrary(standardLibrary);

        return consumer.getMessageHandler();
    }
    
    public ResultStream<String> getLibraryPaths() {

        var templatesList = getResourceAsStream(templatesListFile);
        if (templatesList == null) {
            return ResultStream.of(Result.error("File containing list of templates not found in standard library."));
        }

        List<String> paths = new LinkedList<>();

        try {
            paths.addAll(IOUtils.readLines(templatesList, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            return ResultStream.of(Result.error(ex.getMessage()));
        }
        return ResultStream.innerOf(paths);
    } 
    
    private InputStream getResourceAsStream(String path) {
        return this.getClass().getClassLoader().getResourceAsStream(standardLibFolder + "/" + path);
    }

    private void loadFormats() {
        for (StandardFormat format : StandardFormat.values()) {
            this.registerFormat(format.format);
        }
    }

    public TemplateStore getStandardLibrary() {
        return getTemplateStore().getStandardLibrary().get();
    }
}
