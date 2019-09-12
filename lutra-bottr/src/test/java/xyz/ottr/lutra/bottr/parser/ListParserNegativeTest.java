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
public class ListParserNegativeTest {

    ListParser parser = new ListParser('[', ']', ",");

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {

        return Arrays.asList(
            new Object[][]{
                { "[" },
                { "]" },
                { "[[" },
                { "]]" },
                { "][" },
                { "[][]" },
                { "[]]" },
                { "[][]]" },

                { "[ ] asdf"},
                { "asdf [ ] "},

                { "[[a, b, c]" },
                { "[a]]" },
                { "[[a], [b]]]" },
            });
    }

    private final String list;

    public ListParserNegativeTest(String list) {
        this.list = list;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSuccess() {
        test(this.list);
    }

    public void test(String value) {
        List list = this.parser.toList(value);
        Assert.assertEquals(value, list.toString());
    }

}
