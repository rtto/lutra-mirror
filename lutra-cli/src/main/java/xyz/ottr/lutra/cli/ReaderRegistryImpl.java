package xyz.ottr.lutra.cli;

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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.io.ReaderRegistry;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.MessageHandler;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.stottr.io.SFileReader;
import xyz.ottr.lutra.stottr.io.SInstanceParser;
import xyz.ottr.lutra.stottr.io.STemplateParser;
import xyz.ottr.lutra.tabottr.parser.ExcelReader;
import xyz.ottr.lutra.wottr.io.RDFFileReader;
import xyz.ottr.lutra.wottr.parser.v04.WInstanceParser;
import xyz.ottr.lutra.wottr.parser.v04.WTemplateParser;

public class ReaderRegistryImpl implements ReaderRegistry {
    
    private static final ReaderRegistry INSTANCE = new ReaderRegistryImpl();
    
    private Map<String, TemplateReader> templateReaders;
    private Map<String, InstanceReader> instanceReaders;
    
    private ReaderRegistryImpl() {
        this.templateReaders = new LinkedHashMap<>(); // use linked hash maps for predictable order
        this.instanceReaders = new LinkedHashMap<>();
        
        // Add template readers
        
        // wottr
        registerTemplateReader(new TemplateReader(
            new RDFFileReader(), new WTemplateParser(),
            Settings.Format.wottr.toString()));

        // stottr
        registerTemplateReader(new TemplateReader(
            new SFileReader(), new STemplateParser(),
            Settings.Format.stottr.toString()));

        // legacy
        registerTemplateReader(new TemplateReader(
            new RDFFileReader(), new xyz.ottr.lutra.wottr.parser.v03.WTemplateParser(),
            Settings.Format.legacy.toString()));

        // Add instance readers
        
        // wottr
        registerInstanceReader(new InstanceReader(
            new RDFFileReader(), new WInstanceParser(),
            Settings.Format.wottr.toString()));

        // stottr
        registerInstanceReader(new InstanceReader(
            new SFileReader(), new SInstanceParser(),
            Settings.Format.stottr.toString()));
        
        // tabottr
        registerInstanceReader(new InstanceReader(
            new ExcelReader(),
            Settings.Format.tabottr.toString()));

        // legacy
        registerInstanceReader(new InstanceReader(
            new RDFFileReader(), new xyz.ottr.lutra.wottr.parser.v03.WInstanceParser(),
            Settings.Format.legacy.toString()));

    }
    
    public static ReaderRegistry getReaderRegistry() {
        return INSTANCE;
    }
    
    @Override
    public void registerTemplateReader(TemplateReader reader) {
        this.templateReaders.put(reader.getFormat(), reader);
    }

    @Override
    public void registerInstanceReader(InstanceReader reader) {
        this.instanceReaders.put(reader.getFormat(), reader);
    }

    @Override
    public Map<String, TemplateReader> getAllTemplateReaders() {
        return Collections.unmodifiableMap(this.templateReaders);
    }

    public Map<String, InstanceReader> getAllInstanceReaders() {
        return Collections.unmodifiableMap(this.instanceReaders);
    }
    
    public Result<TemplateReader> attemptAllReaders(Function<TemplateReader, MessageHandler> readerFunction) {
        
        Result<TemplateReader> unsuccessful = Result.empty(); // Return in case of no succeed
        for (Map.Entry<String, TemplateReader> reader : getAllTemplateReaders().entrySet()) {
            MessageHandler msgs = readerFunction.apply(reader.getValue());

            if (Message.moreSevere(msgs.getMostSevere(), Message.ERROR)) {
                msgs.toSingleMessage("Attempt of parsing templates as "
                    + reader.getKey() + " format failed:")
                    .ifPresent(unsuccessful::addMessage);
            } else {
                Result<TemplateReader> readerRes = Result.of(reader.getValue());
                msgs.toSingleMessage("")
                    .ifPresent(readerRes::addMessage);
                return readerRes;
            }
        }
        return unsuccessful;
    }
}
