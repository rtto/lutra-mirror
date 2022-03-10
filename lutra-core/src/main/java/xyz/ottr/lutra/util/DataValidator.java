package xyz.ottr.lutra.util;

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

import java.util.Locale;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.irix.IRIs;
import org.apache.jena.irix.IRIx;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;

public class DataValidator {

    // private static final Set<String> DEFAULT_SCHEMES = Set.of("http", "https", "sftp", "ftp", "file", "urn");

    private static Result<IRIx> asIRIx(String value) {
        try {
            return Result.of(IRIs.reference(value));
        } catch (org.apache.jena.irix.IRIException ex) {
            return Result.error("Invalid IRI: " + value + ". " + ex.getMessage());
        }
    }

    public static Result<String> asURI(String value) {
        return asIRIx(value).map(IRIx::str);
        /* Check if scheme is ok. Use IRIs.getScheme(value), which requires a more recent version of Jena?
            if (DEFAULT_SCHEMES.contains(IRIs.toLowerCase(Locale.getDefault()))) {
                result.addMessage(Message.warning("Uncommon scheme for URI: " + value
                + ". Registered common schemes are: " + DEFAULT_SCHEMES));
        }
        */
    }

    public static Result<String> asAbsoluteURI(String value) {
        return asIRIx(value).filter(IRIx::isAbsolute).map(IRIx::str);
    }

    public static Result<String> asLanguageTagString(String value) {
        var result = Result.of(value);
        Locale locale = Locale.forLanguageTag(value.replace("-", "_"));
        if (!LocaleUtils.isAvailableLocale(locale)) {
            result.addMessage(Message.warning("Invalid language tag. Value " + value + " is not a valid language tag."));
        }
        return result;
    }

    public static boolean isLanguageTag(String value) {
        if (isEmpty(value)) {
            return false;
        }
        // first character must be a letter:
        if (!isASCIIAlpha(value.charAt(0))) {
            return false;
        }
        // only alphanumerics or '-' allowed:
        for (int i = 1; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!(isASCIIAlphaNumeric(c) || c == '-')) {
                return false;
            }
        }
        return true;
    }

    public static boolean isChar(String value) {
        return value.length() == 1;
    }

    public static Result<Character> asChar(String value) {
        var result = Result.of(value.charAt(0));
        if (isChar(value)) {
            result.addMessage(Message.error("Invalid character. Value " + value + " is not a character."));
        }
        return result;
    }

    /*
    public static Result<Integer> asInteger(String value) {
        try {
            return Result.of(Integer.valueOf(value));
        } catch (NumberFormatException ex) {
            return getError(value, "integer", ex.getMessage());
        }
    }

    public static Result<Double> asDouble(String value) {
        try {
            return Result.of(Double.valueOf(value));
        } catch (NumberFormatException ex) {
            return getError(value, "double", ex.getMessage());
        }
    }

    public static Result<Boolean> asBoolean(String value) {
        if (value.equalsIgnoreCase("true")) {
            return Result.of(Boolean.TRUE);
        } else if (value.equalsIgnoreCase("false")) {
            return Result.of(Boolean.FALSE);
        } else {
            return getError(value, "boolean");
        }
    }
    */

    public static boolean isEmpty(String value) {
        return StringUtils.isEmpty(value);
    }

    public static boolean isBlank(String value) {
        return StringUtils.isBlank(value);
    }

    public static boolean isDecimal(String value) {
        int dot = value.indexOf(".");
        return
            // there must be a dot:
            dot != -1
                // an integer before the dot:
                && isInteger(value.substring(0, dot), true)
                // a positive integer after the dot:
                && isInteger(value.substring(dot + 1), false);
    }

    public static boolean isInteger(String value) {
        return isInteger(value, true);
    }

    private static boolean isInteger(String value, boolean allowNegative) {
        if (isBlank(value)) {
            return false;
        }
        int length = value.length();
        int i = 0;
        // possibly a minus
        if (allowNegative && value.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        // only digits allowed:
        for (; i < length; i++) {
            char c = value.charAt(i);
            if (!isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isASCIILowercase(char c) {
        return c >= 'a' && c <= 'z';
    }

    private static boolean isASCIIUppercase(char c) {
        return c >= 'A' && c <= 'Z';
    }

    private static boolean isASCIIAlpha(char c) {
        return isASCIILowercase(c) || isASCIIUppercase(c);
    }

    private static boolean isASCIIAlphaNumeric(char c) {
        return isDigit(c) || isASCIIAlpha(c);
    }

}