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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.stottr.io.SFileReader;
import xyz.ottr.lutra.stottr.io.SInstanceParser;
import xyz.ottr.lutra.stottr.io.STemplateParser;
import xyz.ottr.lutra.tabottr.parser.ExcelReader;
import xyz.ottr.lutra.wottr.io.WFileReader;
import xyz.ottr.lutra.wottr.io.WInstanceParser;
import xyz.ottr.lutra.wottr.io.WTemplateParser;

public class ReaderRegistry {
    
    private static Map<Settings.Format, TemplateReader> templateReaders = new HashMap<>();
    private static Map<Settings.Format, InstanceReader> instanceReaders = new HashMap<>();
    private static Map<TemplateReader, Settings.Format> templateReaderFormat = new HashMap<>();
    private static Map<InstanceReader, Settings.Format> instanceReaderFormat = new HashMap<>();
    
    static {

        // Add template readers
        
        // wottr
        TemplateReader wottrTemplateReader = 
                new TemplateReader(new WFileReader(), new WTemplateParser());
        templateReaders.put(Settings.Format.wottr, wottrTemplateReader);
        templateReaderFormat.put(wottrTemplateReader, Settings.Format.wottr);

        // legacy
        TemplateReader legacyTemplateReader =
                new TemplateReader(new xyz.ottr.lutra.wottr.legacy.io.WFileReader(),
                        new xyz.ottr.lutra.wottr.legacy.io.WTemplateParser());
        templateReaders.put(Settings.Format.legacy, legacyTemplateReader);
        templateReaderFormat.put(legacyTemplateReader, Settings.Format.legacy);

        // stottr

        TemplateReader stottrTemplateReader = 
                new TemplateReader(new SFileReader(), new STemplateParser());
        templateReaders.put(Settings.Format.stottr, stottrTemplateReader);
        templateReaderFormat.put(stottrTemplateReader, Settings.Format.stottr);
        
        // Add instance readers
        
        // wottr
        InstanceReader wottrInstanceReader = 
                new InstanceReader(new WFileReader(), new WInstanceParser());
        instanceReaders.put(Settings.Format.wottr, wottrInstanceReader);
        instanceReaderFormat.put(wottrInstanceReader, Settings.Format.wottr);
        
        // legacy
        InstanceReader legacyInstanceReader = 
                new InstanceReader(new xyz.ottr.lutra.wottr.legacy.io.WFileReader(),
                        new xyz.ottr.lutra.wottr.legacy.io.WInstanceParser());
        instanceReaders.put(Settings.Format.legacy, legacyInstanceReader);
        instanceReaderFormat.put(legacyInstanceReader, Settings.Format.legacy);
        
        // stottr
        InstanceReader stottrInstanceReader =
                new InstanceReader(new SFileReader(), new SInstanceParser());
        instanceReaders.put(Settings.Format.stottr, stottrInstanceReader);
        instanceReaderFormat.put(stottrInstanceReader, Settings.Format.stottr);
        
        // tabottr
        InstanceReader tabInstanceReader = new InstanceReader(new ExcelReader());
        instanceReaders.put(Settings.Format.tabottr, tabInstanceReader);
        instanceReaderFormat.put(tabInstanceReader, Settings.Format.tabottr);
    }
    
    public static Result<List<TemplateReader>> getTemplateReaders(Settings.Format format) {
        if (format != null) {
            if (!templateReaders.containsKey(format)) {
                return Result.empty(Message.error(
                        "Format " + format + " not yet supported as input format for templates."));
            }
            return Result.of(Arrays.asList(templateReaders.get(format)));
        } else {
            return Result.of(new LinkedList<>(ReaderRegistry.getAllTemplateReaders().values()));
        }
    }
    
    public static Result<InstanceReader> getInstanceReader(Settings.Format format) {
        if (!instanceReaders.containsKey(format)) {
            return Result.empty(Message.error(
                "Format " + format + " not yet supported as input format for instances."));
        }
        return Result.of(instanceReaders.get(format));
    }
    
    public static Settings.Format getTemplateReaderFormat(TemplateReader reader) {
        return templateReaderFormat.get(reader);
    }
    
    public static Settings.Format getInstanceReaderFormat(InstanceReader reader) {
        return instanceReaderFormat.get(reader);
    }
    
    protected static void registerTemplateReader(Settings.Format format, TemplateReader reader) {
        templateReaders.put(format, reader);
    }

    protected static void registerInstanceReader(Settings.Format format, InstanceReader reader) {
        instanceReaders.put(format, reader);
    }

    public static Map<Settings.Format, TemplateReader> getAllTemplateReaders() {
        return templateReaders;
    }

    public static Map<Settings.Format, InstanceReader> getAllInstanceReaders() {
        return instanceReaders;
    }
}
