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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.ottr.lutra.bottr.util.ListParser;

public class ListParserPositiveTest {

    ListParser parser = new ListParser('[', ']', ",");

    @ParameterizedTest
    @MethodSource("listToSize")
    public void test(String list, int expectedSize) {
        int actualSize = this.parser.toList(list).size();
        assertEquals(expectedSize, actualSize);
    }

    public static Stream<Arguments> listToSize() {

        return Stream.of(
                arguments("[a, b, c]", 3),
                arguments("[a]", 1),
                arguments("[]", 0),

                arguments("", 0),
                arguments(",,,", 4),
                arguments(",", 2),

                arguments("[[[a]]]", 1),
                arguments("[[a   ], [ b ]]", 2),
                arguments("[[a], [b], [c]]", 3),
                arguments("[[a, b, c], [d, e, f], [g, h]]", 3),
                arguments("[[a, b, c], e, [d, e, f], [g, h]]", 4),

                // testing empty elements
                arguments("[, ]", 2),
                arguments("[, , ]", 3),
                arguments("[[a, b, c], e, , [d, e, f], [g, h]]", 5),
                arguments("[a, ]", 2),
                arguments("[[a], [b], [c], ]", 4)
        );
    }

}
