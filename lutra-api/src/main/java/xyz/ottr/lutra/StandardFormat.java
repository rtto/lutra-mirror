package xyz.ottr.lutra;

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

import java.util.Locale;

import xyz.ottr.lutra.bottr.BottrFormat;
import xyz.ottr.lutra.io.Format;
import xyz.ottr.lutra.stottr.StottrFormat;
import xyz.ottr.lutra.tabottr.TabottrFormat;
import xyz.ottr.lutra.wottr.WottrFormat;

public enum StandardFormat {

    // Note: the enum name must ignore-case-match Format.getFormatName().

    wottr(new WottrFormat()),
    stottr(new StottrFormat()),
    tabottr(new TabottrFormat()),
    bottr(new BottrFormat());

    public final Format format;

    StandardFormat(Format f) {
        this.format = f;
        if (!f.getFormatName().equalsIgnoreCase(this.name())) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " " + name()
                + " does not match format name " + f.getFormatName() + ".");
        }
    }

    public String toString() {
        return this.format.getFormatName().toLowerCase(Locale.getDefault());
    }
}
