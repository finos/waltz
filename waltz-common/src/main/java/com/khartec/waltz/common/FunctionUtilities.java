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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Collection of utility methods to make working with functions simpler and more expressive
 */
public class FunctionUtilities {

    private static final Logger LOG = LoggerFactory.getLogger(FunctionUtilities.class);

    /**
     * This odd looking function can be used as a wrapper
     * to explicitly discard a result of another function,
     * effectively turning the wrapped functions return type
     * into void.
     * @param x - the parameter to discard
     */
    public static void discardResult(Object x) {
    }


    /**
     * Intended to be used to do ad-hoc informal timings of functions.
     * The time taken by the function is logged out (info level) and the
     * natural result of the timed function is returned, allowing this
     * utility method to be easily introduced.
     *
     * <pre>
     * String r = possiblySlowCalc(x, y);
     *
     * // => becomes
     *
     * String r = FunctionUtilities.time("timing possiblySlowCalc", () -> possibleSlowCalc(x, y));
     * </pre>
     *
     * @param name The name to use when logging timing information
     * @param supplier The function that performs the calculation to be timed
     * @param <T> Return type of the supplier
     * @return The value produced by the supplier
     */
    public static <T> T time(String name, Supplier<T> supplier) {
        long st = System.currentTimeMillis();

        try {
            T r = supplier.get();
            long end = System.currentTimeMillis();

            LOG.info("duration [{}]: {}", name, (end - st));
            return r;
        } catch (Exception e) {
            String msg = String.format("Unexpected error when timing [%s]: %s", name, e.getMessage());
            LOG.error(msg, e);
            return null;
        }

    }

    public static void time(String name, Runnable runner) {
        long st = System.currentTimeMillis();
        try {
            runner.run();
            long end = System.currentTimeMillis();
            LOG.info("duration [{}]: {}", name, (end - st));
        } catch (Exception e) {
            String msg = String.format("Unexpected error when timing [%s]: %s", name, e.getMessage());
            LOG.error(msg, e);
        }
    }


    /**
     * Returns a `BiFunction` which always ignores its arguments and returns
     * a constant
     * @param result The constant value to return
     * @param <X> Type of argument 1
     * @param <Y> Type of argument 2
     * @param <Z> Type of return value
     * @return Always returns the `result` object
     */
    public static <X, Y, Z> BiFunction<X, Y, Z> alwaysBi(Z result) {
        return (x, y) -> result;
    }


    /**
     * Returns a `Function` which always ignores its arguments and returns
     * a constant
     * @param result The constant value to return
     * @param <X> Type of argument 1
     * @param <Y> Type of return value
     * @return Always returns the `result` object
     */
    public static <X, Y> Function<? super X, Y> always(Y result) {
        return (x) -> result;
    }
}
