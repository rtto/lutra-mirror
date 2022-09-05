package xyz.ottr.lutra.io;

/*-
 * #%L
 * xyz.ottr.lutra:lutra-core
 * %%
 * Copyright (C) 2018 - 2021 University of Oslo
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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import xyz.ottr.lutra.store.StandardTemplateStore;
import xyz.ottr.lutra.store.TemplateStore;

public class FormatManagerTest {

    /**
     *  Test fix for issue #228
     *  Isuue #228: FormatManager.attemptAllFormats does nothing if no formats registered
     *  Fix: returns message stating no formats registered
     */
    @Test
    public void testAttemptAllFormats_emptyFormats() {
        FormatManager formatManager = new FormatManager();
        TemplateStore store = new StandardTemplateStore(formatManager);

        String expectedResponse = "No formats registered to FormatManager";
        String actualResponse = formatManager.attemptAllFormats(store, null).getAllMessages().get(0).getMessage();
        assertEquals(expectedResponse, actualResponse);
    }

}
