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
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.jena.ext.xerces.util.URI;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;

public class DataValidator {

    private static final String[] DEFAULT_SCHEMES = { "http", "https", "ftp", "file" };
    private static final UrlValidator validator = new UrlValidator(DEFAULT_SCHEMES);

    public static Result<String> asURL(String value) {
        return isURL(value)
            ? Result.of(value)
            : Result.error("Invalid URL. Value " + value + " is not a valid URL.");
    }

    public static boolean isURL(String value) {
        return validator.isValid(value);
    }

    public static Result<String> asURI(String value) {

        var result = Result.of(value);
        try {
            new URI(value);
        } catch (URI.MalformedURIException ex) {
            result.addMessage(Message.warning("Suspicious IRI. " + value + ". " + ex.getMessage() + "."));
        }
        return result;
    }

    public static Result<String> asLanguageTagString(String value) {

        var result = Result.of(value);
        Locale locale = Locale.forLanguageTag(value.replace("-", "_"));
        if (!LocaleUtils.isAvailableLocale(locale)) {
            result.addMessage(Message.warning("Invalid language tag. Value " + value + " is not a valid language tag."));
        }
        return result;
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

}
