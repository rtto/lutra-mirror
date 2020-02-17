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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.shared.PrefixMapping;

import xyz.ottr.lutra.bottr.BottrFormat;
import xyz.ottr.lutra.io.Format;
import xyz.ottr.lutra.stottr.StottrFormat;
import xyz.ottr.lutra.tabottr.TabottrFormat;
import xyz.ottr.lutra.wottr.LegacyFormat;
import xyz.ottr.lutra.wottr.WottrFormat;

public class FormatUtils {
    
    private final Map<Settings.FormatName, Format> formats;
    
    public FormatUtils(PrefixMapping prefixes) {
        this.formats = new HashMap<>();
        registerFormats(prefixes);
    }
    
    public Format getFormat(Settings.FormatName formatName) {
        return this.formats.get(formatName);
    }
    
    public Collection<Format> getFormats() {
        return this.formats.values();
    }
    
    private void registerFormats(PrefixMapping prefixes) {
        this.formats.put(Settings.FormatName.wottr, new WottrFormat(prefixes));
        this.formats.put(Settings.FormatName.legacy, new LegacyFormat());
        this.formats.put(Settings.FormatName.stottr, new StottrFormat(prefixes.getNsPrefixMap()));
        this.formats.put(Settings.FormatName.tabottr, new TabottrFormat());
        this.formats.put(Settings.FormatName.bottr, new BottrFormat());
    }
}
