package xyz.ottr.lutra.tabottr;

/*-
 * #%L
 * xyz.ottr.lutra:lutra-tabottr
 * %%
 * Copyright (C) 2018 - 2023 University of Oslo
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
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.writer.InstanceWriter;

public class TabottrFormatTest {

    @Test
    public void support() {
        TabottrFormat format = new TabottrFormat();

        assertTrue(format.supportsInstanceReader());
        assertFalse(format.supportsInstanceWriter());
        assertFalse(format.supportsTemplateReader());
        assertFalse(format.supportsTemplateWriter());
    }

    @Test
    public void getInstanceWriter() {
        String expected = "Unsupported format operation";
        TabottrFormat format = new TabottrFormat();
        Result<InstanceWriter> result = format.getInstanceWriter();
        Assertions.containsMessageFragment(result.getMessageHandler(), Message.Severity.ERROR, expected);
    }

}
