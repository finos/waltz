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

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
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


    public static void dump(String preamble, StreamUtilities.Siphon<?> siphon) {
        dump(preamble, siphon, Function.identity());
    }


    public static <V> void dump(String preamble, StreamUtilities.Siphon<V> siphon, Function<V, ?> formatter) {
        System.out.printf(
                "%s - %s\n",
                preamble,
                ListUtilities.map(siphon.getResults(), formatter));
    }
}
