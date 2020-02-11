package xyz.ottr.lutra.cli;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.shared.PrefixMapping;

import xyz.ottr.lutra.OTTR;

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

import xyz.ottr.lutra.bottr.BottrFormat;
import xyz.ottr.lutra.io.Format;
import xyz.ottr.lutra.io.FormatManager;
import xyz.ottr.lutra.stottr.StottrFormat;
import xyz.ottr.lutra.tabottr.TabottrFormat;
import xyz.ottr.lutra.wottr.LegacyFormat;
import xyz.ottr.lutra.wottr.WottrFormat;

public class FormatUtils {
    
    private final Map<Settings.FormatName, Format> formats;
    private final FormatManager formatManager;
    private final PrefixMapping prefixes;
    
    public FormatUtils() {
        this.formats = new HashMap<>();
        this.formatManager = new FormatManager();
        this.prefixes = PrefixMapping.Factory.create();
        this.prefixes.setNsPrefixes(OTTR.getDefaultPrefixes());
        registerFormats();
    }
    
    public Format getFormat(Settings.FormatName formatName) {
        return this.formats.get(formatName);
    }
    
    public FormatManager getFormatManager() {
        return this.formatManager;
    }
    
    public void addPrefixes(PrefixMapping newPrefixes) {
        this.prefixes.setNsPrefixes(newPrefixes);
    }

    private void registerFormats() {
        
        this.formatManager.register(new WottrFormat(this.prefixes));
        this.formatManager.register(new LegacyFormat());
        this.formatManager.register(new StottrFormat(this.prefixes.getNsPrefixMap()));
        this.formatManager.register(new TabottrFormat());
        this.formatManager.register(new BottrFormat());
    }
}
