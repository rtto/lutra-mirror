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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.api.StandardTemplateManager;
import xyz.ottr.lutra.system.Assertions;

public class StandardTemplateManagerTest {

    @Test
    public void checkInitialisedLibrary() {

        var manager = new StandardTemplateManager();
        var msgs = manager.loadStandardTemplateLibrary();

        // check that the templates loaded without errors
        Assertions.noErrors(msgs);

        // the template store should be empty
        assertTrue(manager.getTemplateStore().getTemplateIRIs().isEmpty());

        // check the contents of the standard library
        var templates = manager.getStandardLibrary().getTemplateIRIs();

        // check that there are templates at all
        assertFalse(templates.isEmpty());

        // check that no templates have an IRI starting with the package path
        assertTrue(templates.stream().noneMatch((String s) -> s.startsWith(OTTR.ns_library_package)));

        // check that there are templates from rdf, rdfs, and owl
        assertTrue(templates.stream().anyMatch(s -> s.startsWith(OTTR.ns_library + "/rdf/")));
        assertTrue(templates.stream().anyMatch(s -> s.startsWith(OTTR.ns_library + "/rdfs/")));
        assertTrue(templates.stream().anyMatch(s -> s.startsWith(OTTR.ns_library + "/owl/")));
    }

}
