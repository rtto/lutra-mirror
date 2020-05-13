package xyz.ottr.lutra.stottr;

/*-
 * #%L
 * lutra-stottr
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

import java.util.Collection;
import java.util.Set;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.io.Format;
import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.stottr.io.SFileReader;
import xyz.ottr.lutra.stottr.parser.SInstanceParser;
import xyz.ottr.lutra.stottr.parser.STemplateParser;
import xyz.ottr.lutra.stottr.writer.SInstanceWriter;
import xyz.ottr.lutra.stottr.writer.STemplateWriter;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.writer.InstanceWriter;
import xyz.ottr.lutra.writer.TemplateWriter;

public class StottrFormat implements Format {

    private static final String name = "stOTTR";
    private static final Collection<Support> support = Set.of(
        Support.TemplateReader,
        Support.TemplateWriter,
        Support.InstanceReader,
        Support.InstanceWriter);

    private PrefixMapping prefixes;

    public StottrFormat() {
        this(OTTR.getDefaultPrefixes());
    }
    
    public StottrFormat(PrefixMapping prefixes) {
        this.prefixes = prefixes;
    }

    @Override
    public Result<TemplateReader> getTemplateReader() {
        return Result.of(new TemplateReader(new SFileReader(), new STemplateParser()));
    }

    @Override
    public Result<TemplateWriter> getTemplateWriter() {
        return Result.of(new STemplateWriter(this.prefixes));
    }

    @Override
    public Result<InstanceReader> getInstanceReader() {
        return Result.of(new InstanceReader(new SFileReader(), new SInstanceParser()));
    }

    @Override
    public Result<InstanceWriter> getInstanceWriter() {
        return Result.of(new SInstanceWriter(this.prefixes));
    }

    @Override
    public Collection<Support> getSupport() {
        return support;
    }

    @Override
    public String getDefaultFileSuffix() {
        return ".stottr";
    }

    @Override
    public String getFormatName() {
        return name;
    }

    @Override
    public void setPrefixMapping(PrefixMapping prefixes) {
        this.prefixes = prefixes;
    }

}
