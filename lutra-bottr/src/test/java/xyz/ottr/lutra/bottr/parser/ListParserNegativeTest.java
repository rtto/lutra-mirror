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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.ottr.lutra.bottr.util.ListParser;

public class ListParserNegativeTest {

    private final ListParser parser = new ListParser('[', ']', ",");

    public static Stream<Arguments> data() {

        return Stream.of(
                arguments("["),
                arguments("]"),
                arguments("[["),
                arguments("]]"),
                arguments("]["),
                arguments("[][]"),
                arguments("[]]"),
                arguments("[][]]"),
                arguments("[ ] asdf"),
                arguments("asdf [ ] "),
                arguments("[[a, b, c]"),
                arguments("[a]]"),
                arguments("[[a], [b]]]")
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testSuccess(String value) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> test(value));
    }

    public void test(String value) {
        List list = this.parser.toList(value);
        MatcherAssert.assertThat(list.toString(), is(value));
    }

}
