package xyz.ottr.lutra.bottr.parser;

/*-
 * #%L
 * lutra-bottr
 * %%
 * Copyright (C) 2018 - 2019 University of Oslo
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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import xyz.ottr.lutra.bottr.util.ListParser;

@RunWith(Parameterized.class)
public class ListParserPositiveTest {

    ListParser parser = new ListParser('[', ']', ",");

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {

        return Arrays.asList(
            new Object[][]{
                { "[a, b, c]", 3 },
                { "[a]", 1 },
                { "[]", 0 },

                { "", 0},
                { ",,,", 4 },
                { ",", 2 },

                { "[[[a]]]", 1 },
                { "[[a   ], [ b ]]", 2 },
                { "[[a], [b], [c]]", 3 },
                { "[[a, b, c], [d, e, f], [g, h]]", 3 },
                { "[[a, b, c], e, [d, e, f], [g, h]]", 4 },

                // testing empty elements
                { "[, ]", 2 },
                { "[, , ]", 3 },
                { "[[a, b, c], e, , [d, e, f], [g, h]]", 5 },
                { "[a, ]", 2 },
                { "[[a], [b], [c], ]", 4 }
            });
    }

    private final int size;
    private final String list;

    public ListParserPositiveTest(String list, int size) {
        this.list = list;
        this.size = size;
    }

    @Test public void testSuccess() {
        test(this.list, this.size);
    }

    public void test(String value, int size) {
        List list = this.parser.toList(value);
        Assert.assertEquals(size, list.size());
    }

}
