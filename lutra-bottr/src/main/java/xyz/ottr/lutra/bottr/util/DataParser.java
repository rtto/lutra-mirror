package xyz.ottr.lutra.bottr.util;

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

import org.apache.jena.ext.xerces.util.URI;
import xyz.ottr.lutra.result.Result;

// TODO suggest to move this to core.parser or core.util

public enum DataParser {

    ; // singleton enum utility class

    public static Result<URI> asURI(String value) {
        try {
            return Result.of(new URI(value));
        } catch (URI.MalformedURIException ex) {
            return getError(value, "valid IRI", ex.getMessage());
        }
    }

    public static Result<String> asLanguageTagString(String value) {

        Locale locale = new Locale.Builder().setLanguageTag(value).build();
        return LocaleUtils.isAvailableLocale(locale)
            ? Result.of(value)
            : getError(value, "language tag");
    }

    public static Result<Character> asChar(String value) {
        return value.length() == 1
            ? Result.of(value.charAt(0))
            : getError(value, "character");
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

    private static Result getError(Object value, String valueType) {
        return getError(value, valueType, null);
    }

    private static Result getError(Object value, String valueType, String message) {
        String errorMessage = "Value " + value + " is not a " + valueType;
        if (StringUtils.isNotEmpty(message)) {
            errorMessage += ": " + message;
        }
        errorMessage += ".";
        return Result.error(errorMessage);
    }



}
