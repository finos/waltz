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
     * @return
     */
    public static <T> T time(String name, Supplier<T> supplier) {
        // LOG.info("-- begin [" + name + "]");

        long st = System.currentTimeMillis();

        try {
            T r = supplier.get();
            long end = System.currentTimeMillis();

            // LOG.info("-- end [" + name + "]");
            LOG.info("-- dur [" + name + "]: " + (end - st));
            //LOG.info("-- result [" + name + "]: " + r);
            return r;
        } catch (Exception e) {
            String msg = String.format("Unexpected error when timing [%s]: %s", name, e.getMessage());
            LOG.error(msg, e);
            return null;
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
    public static <X, Y> Function<X, Y> always(Y result) {
        return (x) -> result;
    }
}
