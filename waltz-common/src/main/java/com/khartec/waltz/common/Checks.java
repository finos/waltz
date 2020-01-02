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

package com.khartec.waltz.common;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

public class Checks {

    /**
     * @param t Value to check
     * @param message Text  to use in the exception message (can use standard string formatting)
     * @param args Arguments to use in format
     * @param <T> Type of the value
     * @return The given value, facilitates method chaining
     * @throws IllegalArgumentException If the value <code>t</code> is null
     */
    public static <T> T checkNotNull(T t, String message, Object... args) {
        checkTrue(t != null, message, args);
        return t;
    }


    /**
     * Verifies all elements of an array comply to a given predicate.
     * @param ts Array of elements
     * @param check Predicate function used to check each element
     * @param message Text to use in any exception message
     * @param <T> Type of the elements
     * @return The given array
     * @throws IllegalArgumentException If any of the elements fail the predicate
     */
    public static <T> T[] checkAll(T[] ts, Predicate<T> check, String message) {
        checkNotNull(ts, message + ": Array was null");
        checkNotNull(check, message + ": Predicate was null");

        checkTrue(ArrayUtilities.all(ts, check), message);
        return ts;
    }

/**
     * Verifies all elements of an collection comply to a given predicate.
     * @param ts Collections of elements
     * @param check Predicate function used to check each element
     * @param message Text to use in any exception message
     * @param <T> Type of the elements
     * @return The given collection
     * @throws IllegalArgumentException If any of the elements fail the predicate
     */
    public static <T> Collection<T> checkAll(Collection<T> ts, Predicate<T> check, String message) {
        checkNotNull(ts, message + ": Array was null");
        checkNotNull(check, message + ": Predicate was null");

        checkTrue(CollectionUtilities.all(ts, check), message);
        return ts;
    }


    /**
     * Verifies that the boolean <code>b</code> is true
     * @param b Boolean to check
     * @param msg Text to use in any exception message (can use standard string formatting)
     * @param args Arguments to use in format
     * @throws IllegalArgumentException if <code>b != true</code>
     */
    public static void checkTrue(boolean b, String msg, Object... args) {
        if (! b) {
            fail(msg, args);
        }
    }


    public static void checkFalse(boolean b, String message) {
        checkTrue(! b, message);
    }


    public static <T> T checkOptionalIsPresent(Optional<T> optional, String message) {
        return optional
                .orElseThrow(() -> mkFail(message));
    }


    public static <T> void checkNotEmpty(Collection<T> ts, String message) {
        checkNotNull(ts, message);
        checkFalse(ts.isEmpty(), message);
    }


    public static <T> void checkNotEmpty(T[] ts, String message) {
        checkNotNull(ts, message);
        checkFalse(ArrayUtilities.isEmpty(ts), message);
    }


    public static String checkNotEmpty(String str, String message) {
        checkNotNull(str, message);
        checkFalse(str.trim().equals(""), message);
        return str;
    }


    public static void fail(String msg, Object... args) {
        throw mkFail(msg, args);
    }

    public static IllegalArgumentException mkFail(String msg, Object... args) {
        throw new IllegalArgumentException(String.format(msg, args));
    }


}
