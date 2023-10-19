/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.common;

import org.jooq.lambda.tuple.Tuple;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.finos.waltz.common.Checks.checkAll;
import static org.finos.waltz.common.Checks.checkNotNull;
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
        return OptionalUtilities
                .ofNullableOptional(maybeString)
                .map(StringUtilities::isEmpty)
                .orElse(true);
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


    public static boolean isNumericLong(String value) {
        return !isEmpty(value)
                && parseLong(value, null) != null;
    }


    public static String mkSafe(String str) {
        return str == null
                ? ""
                : str;
    }

    public static String safeTrim(String str) {
        return mkSafe(str).trim();
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
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining(separator));
    }


    public static <T> String joinUsing(Collection<T> values, Function<T, String> toStringFn, String separator) {
        return values.stream()
                .map(toStringFn)
                .collect(Collectors.joining(separator));
    }


    public static <T> List<T> splitThenMap(String str, String separator, Function<String, T> itemTransformer) {
        checkNotNull(itemTransformer, "itemTransformer cannot be null");
        if (isEmpty(str) || isEmpty(separator)) { return Collections.emptyList(); }

        return Arrays
                .stream(str.split(separator))
                .map(itemTransformer)
                .collect(toList());
    }


    public static List<String> tokenise(String value) {
        return tokenise(value, " ");
    }


    public static List<String> tokenise(String value, String regex) {
        checkNotNull(value, "value cannot be null");

        String[] split = value.split(regex);

        return Stream.of(split)
                .filter(s -> !s.isEmpty())
                .map(String::trim)
                .collect(Collectors.toList());
    }


    public static String lower(String value) {
        checkNotNull(value, "value cannot be null");
        return value
                .toLowerCase()
                .trim();
    }

    public static String upper(String value) {
        checkNotNull(value, "value cannot be null");
        return value
                .toUpperCase()
                .trim();
    }


    public static char firstChar(String str, char dflt) {
        return mkSafe(str).length() > 0
                ? str.charAt(0)
                : dflt;
    }

    public static Optional<Character> firstChar(String str) {
        return mkSafe(str).length() > 0
                ? Optional.of(str.charAt(0))
                : Optional.empty();
    }


    public static Optional<String> toOptional(String str) {
        return isEmpty(str)
                ? Optional.empty()
                : Optional.of(str);
    }


    /**
     * Given a vararg/array of path segments will join them
     * to make a string representing the path.  No starting or trailing
     * slashes are added to the resultant path string.
     *
     * @param segs Segments to join
     * @return String representing the path produced by joining the segments
     * @throws IllegalArgumentException If any of the segments are null
     */
    public static String mkPath(String... segs) {
        checkAll(
                segs,
                StringUtilities::notEmpty,
                "Cannot convert empty or null segments to path");

        return Stream
                .of(segs)
                .map(Object::toString)
                .collect(Collectors.joining("/"))
                .replaceAll("/+", "/");

    }


    public static String capitalise(String words){
        if (StringUtilities.isEmpty(words)) {
            return "";
        }

        List<String> collect = Arrays
                .stream(words.split("\\s+"))
                .map(w -> {
                    String lower = w.toLowerCase().substring(1);

                    char firstLetter = Character.toUpperCase(w.charAt(0));
                    return firstLetter + lower;
                })
                .collect(Collectors.toList());


        return String.join(" ", collect);
    }

    /**
     * Removes dodgy characters and replaces with similar that can render correctly in Waltz
     *
     * @param str raw string
     * @return sanitized string
     */
    public static String sanitizeCharacters(String str) {
        if (str == null) {
            return null;
        } else {
            return str
                    .replaceAll("[‐–—-]", "-")
                    .replaceAll("[”“]", "\"")
                    .replaceAll("[’‘]", "'")
                    .replaceAll("​", "")
                    .replaceAll("â€œ", "\"")
                    .replaceAll("â€", "\"")
                    .replaceAll("â€™", "'")
                    .replaceAll(" ", " ");
        }
    }


    /**
     * Combines a display name and email address in accordance with mailbox standard defined in RFC5332.
     * Example:
     * <pre>
     *     toMailbox("Fred Bloggs", "fred.blogs@mycorp.com")  //  "Fred Bloggs <fred.bloggs@mycorp.com>"
     * </pre>
     *
     * @param displayName display name that indicates the name of the recipient (which can be a person or a system)
     *                    that could be displayed to the user of a mail application,
     * @param email       associated email address
     * @return string formatted in accordance with mailbox spec
     */
    public static String toMailbox(String displayName, String email) {
        return format("%s <%s>", displayName, email).trim();
    }


    public static String toHtmlTable(List<String> headers,
                                     Collection<? extends Tuple> rows) {
        String rowData = rows
                .stream()
                .map(row -> row
                        .toList()
                        .stream()
                        .map(c -> c == null ? "-" : c.toString())
                        .collect(joining("</td><td>", "<tr><td>", "</td></tr>")))
                .collect(joining("\n"));

        String header = headers
                .stream()
                .collect(joining("</th><th>", "<thead><tr><th>", "</th></tr></thead>"));

        return "<table class='table table-condensed small'>" +
                header +
                "<tbody>" +
                rowData +
                "</tbody>" +
                "</table>";
    }


    /**
     * Takes a string and returns null if the string was empty (spaces, empty string or null)
     *
     * @param str  the string to check
     * @return  null iff the str was empty
     */
    public static String nullIfEmpty(String str) {
        return isEmpty(str)
                ? null
                : str;
    }


    public static boolean isDefined(String str) {
        return str != null && str.trim().length() > 0;
    }


    public static Optional<String> firstNonNull(String... xs) {
        return Stream
                .of(xs)
                .filter(x -> isDefined(x))
                .findFirst();
    }

}

