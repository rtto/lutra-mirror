package xyz.ottr.lutra.util;

/*-
 * #%L
 * lutra-tab
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;

public class DataValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  "})
    public void shouldAcceptBlankStrings(String input) {
        assertTrue(DataValidator.isBlank(input));
    }

    @ParameterizedTest
    @EmptySource
    public void shouldAcceptEmptyStrings(String input)  {
        assertTrue(DataValidator.isEmpty(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "1", "00000", "91234", "007", "-123456789012345678901234567890"})
    public void shouldAcceptIntegers(String value) {
        assertTrue(DataValidator.isInteger(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "1.1", "asdf", "--123", "1-2", "12-", "+ 43", "4 3", " 43", " 43 ", " 43 "})
    public void shouldRejectIntegers(String value) {
        assertFalse(DataValidator.isInteger(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.0", "-1.1", "0.2", "-0.4", "91234.123"})
    public void shouldAcceptDecimals(String value) {
        assertTrue(DataValidator.isDecimal(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "abc", "@", "1", "1.1-", "--123", "1-2", "-54", "-4.6.8",
                            "+ 1234.456", "1 234.456", "1234.456E+2", "123. 45", "123 . 45",
                            " 1.0 ", "1.0 ", " -1.0 "})
    public void shouldRejectDecimals(String value) {
        assertFalse(DataValidator.isDecimal(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-", "v", "Æ", "4", "\n", "\0", " "})
    public void shouldAcceptCharsAndGiveNoErrors(String value) {
        assertTrue(DataValidator.isChar(value));
        Assertions.noErrors(DataValidator.asChar(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "--", "no", "en-GB", "234", " b", "b ", " b "})
    public void shouldRejectCharsAndGiveErrors(String value) {
        assertFalse(DataValidator.isChar(value));
        Result<Character> result = DataValidator.asChar(value);
        assertSame(Message.Severity.ERROR, result.getMessageHandler().getMostSevere());
    }

    @ParameterizedTest
    @ValueSource(strings = {"no", "en-GB", "en-GB-London", "cmn-Hans-CN", "de-CH-1901"})
    public void shouldAcceptLanguageTagsAndGiveNoErrors(String value) {
        assertTrue(DataValidator.isLanguageTag(value));
        Assertions.noErrors(DataValidator.asLanguageTagString(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", ".com", "@", "--123", "1-2", "1", "91.1", "a a", "æøå", "no-", "-no", "en--GB"})
    public void shouldRejectLanguageTagsAndGiveErrors(String value) {
        assertFalse(DataValidator.isLanguageTag(value));
        Result<String> result = DataValidator.asLanguageTagString(value);
        assertSame(Message.Severity.ERROR, result.getMessageHandler().getMostSevere());
    }
}
