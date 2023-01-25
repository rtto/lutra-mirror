package xyz.ottr.lutra.bottr;

/*-
 * #%L
 * xyz.ottr.lutra:lutra-bottr
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

public class BottrFormatTest {

    @Test
    public void support() {
        BottrFormat format = new BottrFormat();

        assertTrue(format.supportsInstanceReader());
        assertFalse(format.supportsInstanceWriter());
        assertFalse(format.supportsTemplateReader());
        assertFalse(format.supportsTemplateWriter());
    }
}
