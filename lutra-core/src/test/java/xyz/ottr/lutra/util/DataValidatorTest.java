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

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.function.Predicate;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;

public class DataValidatorTest {
    
    private static final Logger log = LoggerFactory.getLogger(DataValidatorTest.class);
    
    private void accept(Predicate<String> func, String value) {
        boolean result = func.test(value);
        if (!result) {
            this.log.error("Error testing value: " + value);
        }
        assertTrue(result);
    }
    
    private void reject(Predicate<String> func, String value) {
        accept(func.negate(), value);
    }
    
    @Test
    public void shouldAcceptIntegers() {
        for (String value : new String[] {"0", "1", "00000", "91234", "007", "-123456789012345678901234567890" }) {
            accept(DataValidator::isInteger, value);
        }

        accept(DataValidator::isInteger, " 43"); // fail
        accept(DataValidator::isInteger, "43 "); // fail
        accept(DataValidator::isInteger, "   43  "); // fail

        //accept(DataValidator::isInteger, "+43"); // fail, not implemented
    }

    @Test
    public void shouldRejectIntegers() {
        for (String value : new String[] { "", "  ", "1.1", "asdf", "--123", "1-2", "12-", "+ 43", "4 3" }) {
            reject(DataValidator::isInteger, value);
        }
    }

    @Test
    public void shouldAcceptDecimals() {
        for (String value : new String[] { "1.0", "-1.1", "0.2", "-0.4", "91234.123" }) {
            accept(DataValidator::isDecimal, value);
        }

        accept(DataValidator::isDecimal, "1.0 "); // fail
        accept(DataValidator::isDecimal, " 1.0"); // fail
        accept(DataValidator::isDecimal, " -1.0 "); // fail

        //accept(DataValidator::isDecimal, "+12.456"); // fail, not implemented
        //accept(DataValidator::isDecimal, " -.456"); // fail, not implemented
        //accept(DataValidator::isDecimal, " -456."); // fail, not implemented
    }
    
    @Test
    public void shouldRejectDecimals() {
        for (String value : new String[] { "", "1.1-", "asdf", "--123", "1-2", "12-", "1" ,"-54", "-4.6.8", "@", "+ 1234.456", "1 234.456", "1234.456E+2" }) {
            reject(DataValidator::isDecimal, value);
        }
    }

    // valid char should give no error
    @Test
    public void asCharValidTest() {
        Result<Character> c;
        for (String value : new String[] { "v", "-" ,"4", "\n", "\0", " "}) {
            c = DataValidator.asChar(value);
            Assertions.noErrors(c); // fail
        }
    }

    // invalid input should give error
    @Test
    public void asCharInvalidTest() {
        Result<Character> c;
        for (String value : new String[] {"abc", "--" ,"49"}) {
            c = DataValidator.asChar(value);
            assertSame(Message.Severity.ERROR, c.getMessageHandler().getMostSevere()); // fail
        }
    }

    @Test
    public void shouldAcceptChar() {
        for (String value : new String[] { "-", "Æ" ,"4", "\n", "\0", " "}) {
            accept(DataValidator::isChar, value);
        }
    }

    @Test
    public void shouldRejectChar() {
        for (String value : new String[] { "no", "en-GB", "234", "", "  "}) {
            reject(DataValidator::isChar, value);
        }
    }

    @Test
    public void shouldAcceptLanguageTag() {
        for (String value : new String[] { "no", "en-GB", "en-GB-London"}) {
            accept(DataValidator::isLanguageTag, value);
        }

        accept(DataValidator::isLanguageTag, "en-GB "); // fail
    }
    
    @Test
    public void shouldRejectLanguageTag() {
        for (String value : new String[] { "", ".com", "@", "--123", "1-2", "12-", "1", "a a", "  ", "91234.123", "æøå", null}) {
            reject(DataValidator::isLanguageTag, value);
        }

        reject(DataValidator::isLanguageTag, "no-"); // fail
    }

    // should give warning/error? if the input is invalid
    @Test
    public void asLanguageTagStringTest() {
        for (String value : new String[] { "abcd", "-en-GB", "234", "", ".com", "no-", null}) {
            Result<String> str = DataValidator.asLanguageTagString(value);
            assertSame(Message.Severity.WARNING, str.getMessageHandler().getMostSevere());
        }
    }
}
