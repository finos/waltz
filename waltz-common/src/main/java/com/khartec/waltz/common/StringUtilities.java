/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.common;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static java.util.stream.Collectors.toList;


public class StringUtilities {

    /**
     * Determines if the given string is null or empty (after trimming).
     * <code>notEmpty</code> provides the inverse.
     * @param x the String to check
     * @return true iff x is null or x.trim is empty
     */
    public static boolean isEmpty(String x) {
        return x == null || x.trim().equals("");
    }


    /**
     * Convenience method for <code> ! isEmpty(x) </code>
     * @param x the String to check
     * @return true iff x.trim is non empty
     */
    public static boolean notEmpty(String x) {
        return ! isEmpty(x);
    }


    public static boolean isEmpty(Optional<String> maybeString) {
        if (maybeString == null) return true;
        if (! maybeString.isPresent()) return true;
        return isEmpty(maybeString.get());
    }


    public static String ifEmpty(String x, String defaultValue) {
        return isEmpty(x) ? defaultValue : x;
    }


    public static Long parseLong(String value, Long dflt) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException nfe) {
            return dflt;
        }
    }


    public static Integer parseInteger(String value, Integer dflt) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            return dflt;
        }
    }


    public static String mkSafe(String str) {
        return str == null
                ? ""
                : str;
    }


    public static String limit(String str, int maxLength) {
        if (str == null) return null;
        int howMuch = Math.min(maxLength, str.length());
        return str.substring(0, howMuch);
    }


    public static int length(String str) {
        return str == null
                ? 0
                : str.length();
    }


    public static String join(Collection<?> values, String separator) {
        return values.stream()
                .filter(v -> v != null)
                .map(v -> v.toString())
                .collect(Collectors.joining(separator));
    }


    public static <T> List<T> splitThenMap(String str, String separator, Function<String, T> itemTransformer) {
        if (isEmpty(str) || isEmpty(separator)) { return Collections.emptyList(); }
        if (itemTransformer == null) {
            itemTransformer = s -> (T) s;
        }

        return Arrays
                .stream(str.split(separator))
                .map(itemTransformer)
                .collect(toList());
    }


    public static List<String> tokenise(String value) {
        checkNotNull(value, "value cannot be null");

        String[] split = value.split(" ");

        return Stream.of(split)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }


    public static String lower(String value) {
        checkNotNull(value, "value cannot be null");
        return value
                .toLowerCase()
                .trim();
    }

    public static char firstChar(String str, char dflt) {
        return mkSafe(str).length() > 0
                ? str.charAt(0)
                : dflt;
    }
}
