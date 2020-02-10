package xyz.ottr.lutra.cli;

/*-
 * #%L
 * lutra-cli
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

import xyz.ottr.lutra.bottr.io.BInstanceReader;
import xyz.ottr.lutra.io.FormatManager;
import xyz.ottr.lutra.io.FormatName;
import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.stottr.io.SFileReader;
import xyz.ottr.lutra.stottr.parser.SInstanceParser;
import xyz.ottr.lutra.stottr.parser.STemplateParser;
import xyz.ottr.lutra.tabottr.parser.ExcelReader;
import xyz.ottr.lutra.wottr.io.RDFFileReader;
import xyz.ottr.lutra.wottr.parser.v04.WInstanceParser;
import xyz.ottr.lutra.wottr.parser.v04.WTemplateParser;

public class Utils {

    public static void registerReaders(FormatManager formatManager) {
        
        // Add template readers
        
        // wottr
        formatManager.registerTemplateReader(new TemplateReader(
            new RDFFileReader(), new WTemplateParser(),
            FormatName.wottr));
    
        // stottr
        formatManager.registerTemplateReader(new TemplateReader(
            new SFileReader(), new STemplateParser(),
            FormatName.stottr));
    
        // legacy
        formatManager.registerTemplateReader(new TemplateReader(
            new RDFFileReader(), new xyz.ottr.lutra.wottr.parser.v03.WTemplateParser(),
            FormatName.legacy));
    
        // Add instance readers
        
        // wottr
        formatManager.registerInstanceReader(new InstanceReader(
            new RDFFileReader(), new WInstanceParser(),
            FormatName.wottr));
    
        // stottr
        formatManager.registerInstanceReader(new InstanceReader(
            new SFileReader(), new SInstanceParser(),
            FormatName.stottr));
    
        // bottr
        formatManager.registerInstanceReader(new InstanceReader(
            new BInstanceReader(),
            FormatName.bottr));
        
        // tabottr
        formatManager.registerInstanceReader(new InstanceReader(
            new ExcelReader(),
            FormatName.tabottr));
    
        // legacy
        formatManager.registerInstanceReader(new InstanceReader(
            new RDFFileReader(), new xyz.ottr.lutra.wottr.parser.v03.WInstanceParser(),
            FormatName.legacy));
    }

}
