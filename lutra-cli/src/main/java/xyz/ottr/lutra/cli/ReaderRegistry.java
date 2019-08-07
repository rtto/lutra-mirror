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
    
    static {

        // Add template readers
        
        // wottr
        TemplateReader wottrTemplateReader = new TemplateReader(
                new WFileReader(), new WTemplateParser(), Settings.Format.wottr.toString());
        templateReaders.put(Settings.Format.wottr, wottrTemplateReader);

        // legacy
        TemplateReader legacyTemplateReader = new TemplateReader(
                new xyz.ottr.lutra.wottr.legacy.io.WFileReader(),
                new xyz.ottr.lutra.wottr.legacy.io.WTemplateParser(),
                Settings.Format.legacy.toString());
        templateReaders.put(Settings.Format.legacy, legacyTemplateReader);

        // stottr
        TemplateReader stottrTemplateReader = new TemplateReader(
                new SFileReader(), new STemplateParser(),Settings.Format.stottr.toString());
        templateReaders.put(Settings.Format.stottr, stottrTemplateReader);
        
        // Add instance readers
        
        // wottr
        InstanceReader wottrInstanceReader = new InstanceReader(
                new WFileReader(), new WInstanceParser(), Settings.Format.wottr.toString());
        instanceReaders.put(Settings.Format.wottr, wottrInstanceReader);
        
        // legacy
        InstanceReader legacyInstanceReader = new InstanceReader(
                new xyz.ottr.lutra.wottr.legacy.io.WFileReader(),
                new xyz.ottr.lutra.wottr.legacy.io.WInstanceParser(),
                Settings.Format.legacy.toString());
        instanceReaders.put(Settings.Format.legacy, legacyInstanceReader);
        
        // stottr
        InstanceReader stottrInstanceReader = new InstanceReader(
                new SFileReader(), new SInstanceParser(), Settings.Format.stottr.toString());
        instanceReaders.put(Settings.Format.stottr, stottrInstanceReader);
        
        // tabottr
        InstanceReader tabInstanceReader = new InstanceReader(
                new ExcelReader(), Settings.Format.tabottr.toString());
        instanceReaders.put(Settings.Format.tabottr, tabInstanceReader);
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
