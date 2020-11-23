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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import xyz.ottr.lutra.api.StandardTemplateManager;

public class StandardTemplateManagerTest {

    @Test public void nonEmptyStandardLibrary() {

        var manager = new StandardTemplateManager();
        manager.loadStandardTemplateLibrary();

        assertFalse(manager.getStandardLibrary().getTemplateIRIs().isEmpty());
        assertTrue(manager.getTemplateStore().getTemplateIRIs().isEmpty());
    }

    @Test
    public void doNotLoadPackageTemplates() {
        var manager = new StandardTemplateManager();
        manager.loadStandardTemplateLibrary();

        // check that there are templates at all
        assertFalse(manager.getStandardLibrary().getTemplateIRIs().isEmpty());

        // check that none of these templates has an IRI starting with the package path
        assertTrue(manager.getStandardLibrary().getTemplateIRIs().stream().noneMatch((String s) -> {
            return s.startsWith("http://tpl.ottr.xyz/p/");
        }));
    }
}
