package xyz.ottr.lutra.tabottr.parser;

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

import static org.junit.Assert.assertTrue;

import java.util.function.Predicate;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public void shouldAcceptBooleans() {
        for (String value : new String[] { "TRUE", "FALSE", "true", "false" }) {
            accept(DataValidator::isBoolean, value);
        }
    }
    
    @Test
    public void shouldRejectBooleans() {
        for (String value : new String[] { "True", "yes", "1", "0", "", "asdf" }) {
            reject(DataValidator::isBoolean, value);
        }
    }
    
    @Test
    public void shouldAcceptIntegers() {
        for (String value : new String[] { "1", "-1234", "00000", "0", "91234" }) {
            accept(DataValidator::isInteger, value);
        }
    }
    
    @Test
    public void shouldRejectIntegers() {
        for (String value : new String[] { "", "1.1", "asdf", "--123", "1-2", "12-" }) {
            reject(DataValidator::isInteger, value);
        }
    }
    
    @Test
    public void shouldAcceptDecimals() {
        for (String value : new String[] { "1.0", "-1.1", "0.2", "-0.4", "91234.123" }) {
            accept(DataValidator::isDecimal, value);
        }
    }
    
    @Test
    public void shouldRejectDecimals() {
        for (String value : new String[] { "", "1.1-", "asdf", "--123", "1-2", "12-", "1" }) {
            reject(DataValidator::isDecimal, value);
        }
    }
    
    @Test
    public void shouldAcceptLanguageTag() {
        for (String value : new String[] { "no", "en-GB", "asdf", "aa"}) {
            accept(DataValidator::isLanguageTag, value);
        }
    }
    
    @Test
    public void shouldRejectLanguageTag() {
        for (String value : new String[] { "", ".com", "@", "--123", "1-2", "12-", "1", "a a", " ", "91234.123"}) {
            reject(DataValidator::isLanguageTag, value);
        }
    }
}
