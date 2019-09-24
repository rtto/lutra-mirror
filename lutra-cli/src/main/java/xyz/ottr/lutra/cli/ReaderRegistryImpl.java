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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.io.ReaderRegistry;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.MessageHandler;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.stottr.io.SFileReader;
import xyz.ottr.lutra.stottr.parser.SInstanceParser;
import xyz.ottr.lutra.stottr.parser.STemplateParser;
import xyz.ottr.lutra.tabottr.parser.ExcelReader;
import xyz.ottr.lutra.wottr.io.RDFFileReader;
import xyz.ottr.lutra.wottr.parser.v04.WInstanceParser;
import xyz.ottr.lutra.wottr.parser.v04.WTemplateParser;

public class ReaderRegistryImpl implements ReaderRegistry {
    
    private static final ReaderRegistry INSTANCE = new ReaderRegistryImpl();
    
    private Map<String, TemplateReader> templateReaders;
    private Map<String, InstanceReader> instanceReaders;
    
    private ReaderRegistryImpl() {
        this.templateReaders = new HashMap<>();
        this.instanceReaders = new HashMap<>();
        
        // Add template readers
        
        // wottr
        TemplateReader wottrTemplateReader = new TemplateReader(
                new RDFFileReader(), new WTemplateParser(), Settings.Format.wottr.toString());
        this.templateReaders.put(Settings.Format.wottr.toString(), wottrTemplateReader);

        // legacy
        TemplateReader legacyTemplateReader = new TemplateReader(
                new RDFFileReader(),
                new xyz.ottr.lutra.wottr.parser.v03.WTemplateParser(),
                Settings.Format.legacy.toString());
        this.templateReaders.put(Settings.Format.legacy.toString(), legacyTemplateReader);

        // stottr
        TemplateReader stottrTemplateReader = new TemplateReader(
                new SFileReader(), new STemplateParser(),Settings.Format.stottr.toString());
        this.templateReaders.put(Settings.Format.stottr.toString(), stottrTemplateReader);
        
        // Add instance readers
        
        // wottr
        InstanceReader wottrInstanceReader = new InstanceReader(
                new RDFFileReader(), new WInstanceParser(), Settings.Format.wottr.toString());
        this.instanceReaders.put(Settings.Format.wottr.toString(), wottrInstanceReader);
        
        // legacy
        InstanceReader legacyInstanceReader = new InstanceReader(
                new RDFFileReader(),
                new xyz.ottr.lutra.wottr.parser.v03.WInstanceParser(),
                Settings.Format.legacy.toString());
        this.instanceReaders.put(Settings.Format.legacy.toString(), legacyInstanceReader);
        
        // stottr
        InstanceReader stottrInstanceReader = new InstanceReader(
                new SFileReader(), new SInstanceParser(), Settings.Format.stottr.toString());
        this.instanceReaders.put(Settings.Format.stottr.toString(), stottrInstanceReader);
        
        // tabottr
        InstanceReader tabInstanceReader = new InstanceReader(
                new ExcelReader(), Settings.Format.tabottr.toString());
        this.instanceReaders.put(Settings.Format.tabottr.toString(), tabInstanceReader);
    }
    
    public static ReaderRegistry getReaderRegistry() {
        return INSTANCE;
    }
    
    @Override
    public void registerTemplateReader(String format, TemplateReader reader) {
        this.templateReaders.put(format, reader);
    }

    @Override
    public void registerInstanceReader(String format, InstanceReader reader) {
        this.instanceReaders.put(format, reader);
    }

    @Override
    public Map<String, TemplateReader> getAllTemplateReaders() {
        return this.templateReaders;
    }

    public Map<String, InstanceReader> getAllInstanceReaders() {
        return this.instanceReaders;
    }
    
    public Result<TemplateReader> attemptAllReaders(Function<TemplateReader, MessageHandler> todo) {
        
        Result<TemplateReader> unsuccsessfull = Result.empty(); // Return in case of no succeed
        for (Map.Entry<String, TemplateReader> reader : getAllTemplateReaders().entrySet()) {
            MessageHandler msgs = todo.apply(reader.getValue());

            if (Message.moreSevere(msgs.getMostSevere(), Message.ERROR)) {
                msgs.toSingleMessage("Parsing templates as "
                        + reader.getKey() + " failed with following errors:")
                    .ifPresent(unsuccsessfull::addMessage);
            } else {
                Result<TemplateReader> readerRes = Result.of(reader.getValue());
                msgs.toSingleMessage("").ifPresent(readerRes::addMessage);
                return readerRes;
            }
        }
        return unsuccsessfull;
    }
}
