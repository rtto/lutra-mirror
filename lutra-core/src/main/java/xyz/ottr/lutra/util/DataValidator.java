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

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.irix.IRIs;
import org.apache.jena.irix.IRIx;
import org.apache.jena.riot.web.LangTag;
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

    /* Using Jena's TypeMapper to check if type is registeres. */
    public static boolean isDatatypeURI(String value) {
        return TypeMapper.getInstance().getTypeByName(value) != null;
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
        if (isLanguageTag(value)) {
            return Result.of(value);
        }

        return Result.error("Invalid language tag. Value " + value + " is not a language tag.");
    }

    public static boolean isLanguageTag(String value) {
        if (isEmpty(value)) {
            return false;
        }

        return LangTag.check(value);
    }

    public static boolean isChar(String value) {
        // accept single whitespace and escape sequences ' ', '\n', '\0'
        if (isBlank(value) && value.length() == 1) {
            return true;
        }
        return value.length() == 1;
    }

    public static Result<Character> asChar(String value) {
        if (isChar(value)) {
            return Result.of(value.charAt(0));
        }

        return Result.error("Invalid character. Value " + value + " is not a character.");
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

}