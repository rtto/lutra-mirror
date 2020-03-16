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

import lombok.Getter;
import xyz.ottr.lutra.TemplateManager;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.wottr.WottrFormat;

@Getter
public class StandardTemplateManager extends TemplateManager {
    
    private TemplateStore standardLibrary;
    
    public StandardTemplateManager() {
        this.loadFormats();
    }

    public MessageHandler loadStandardTemplateLibrary() {

        this.standardLibrary = makeDefaultStore(getFormatManager());
        super.getTemplateStore().registerStandardLibrary(standardLibrary);

        var folder = getClass().getClassLoader().getResource("templates-master").getPath();

        TemplateReader reader = getFormat(WottrFormat.name).getTemplateReader().get();
        super.getTemplateStore().registerStandardLibrary(standardLibrary);
        return reader.loadTemplatesFromFolder(this.standardLibrary, folder, new String[] { "ttl" }, new String[] {});
    }
    
    private void loadFormats() {
        for (StandardFormat format : StandardFormat.values()) {
            this.registerFormat(format.format);
        }
    }

    public TemplateStore getStandardLibrary() {
        return this.standardLibrary;
    }
}
