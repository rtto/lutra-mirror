package xyz.ottr.lutra.system;

/*-
 * #%L
 * lutra-core
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

import static org.junit.Assert.assertTrue;
import static xyz.ottr.lutra.system.Message.Severity.ERROR;
import static xyz.ottr.lutra.system.Message.Severity.FATAL;
import static xyz.ottr.lutra.system.Message.Severity.INFO;
import static xyz.ottr.lutra.system.Message.Severity.WARNING;
import static xyz.ottr.lutra.system.Message.Severity.greatest;
import static xyz.ottr.lutra.system.Message.Severity.least;

import org.junit.Test;

public class MessageSeverityTest {

    @Test
    public void testSeverityComparison() {

        assertTrue(INFO.isLessThan(WARNING));
        assertTrue(INFO.isLessThan(ERROR));
        assertTrue(INFO.isLessThan(FATAL));

        assertTrue(WARNING.isGreaterThan(INFO));
        assertTrue(WARNING.isLessThan(ERROR));
        assertTrue(WARNING.isLessThan(FATAL));

        assertTrue(ERROR.isGreaterThan(INFO));
        assertTrue(ERROR.isGreaterThan(WARNING));
        assertTrue(ERROR.isLessThan(FATAL));

        assertTrue(FATAL.isGreaterThan(INFO));
        assertTrue(FATAL.isGreaterThan(WARNING));
        assertTrue(FATAL.isGreaterThan(ERROR));

        for (Message.Severity s : Message.Severity.values()) {
            assertTrue(s.isLessEqualThan(greatest()));
            assertTrue(s.isGreaterEqualThan(least()));
        }
    }
}
