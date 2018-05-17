/*
 * Waltz - Enterprise Architecture
 *  Copyright (C) 2016, 2017 Waltz open source project
 *  See README.md for more information
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.common;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

public class DebugUtilities {

    /**
     * Log (stdout) and return the result value, additional var args are given for context.
     *
     * <pre>setSomething(logValue(12, "result of a * b", a, b));</pre>
     *
     * @param result
     * @param ctx
     * @param <R>
     * @return
     */
    public static <R> R logValue(R result, Object... ctx) {
        System.out.println(Arrays.toString(ctx) + "\t=>\t" + result);
        return result;
    }


    /**
     * similar to logValue except the value is computed by the given supplier.
     * Exceptions are reported and the exception is rethrown.
     *
     * <pre>setSomething(logValue(() => a * b, "result of a * b", a, b));</pre>
     *
     * @param resultFn
     * @param ctx
     * @param <R>
     * @return
     */
    public static <R> R logValue(Supplier<R> resultFn, Object... ctx) {
        try {
            R r = resultFn.get();
            System.out.println(Arrays.toString(ctx) + "\t=>\t" + r);
            return r;
        } catch (Exception e) {
            System.out.println(Arrays.toString(ctx) + "\t=> !!\t" + e.getMessage());
            throw e;
        }
    }


    /**
     * Dumps map to stdout.
     */
    public static <K, V> Map<K, V> dump(Map<K, V> m) {
        m.forEach((k, v) -> System.out.println(k + " -> " + v));
        return m;
    }
}
