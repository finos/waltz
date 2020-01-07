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
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.khartec.waltz.common.Checks.checkNotNull;

public class ArrayUtilities {


    /**
     * @throws IllegalArgumentException If either <code>ts</code>
     * or <code>check</code> is null
     * @param ts  array of elements of type <code>T</code>
     * @param check  predicate function
     * @param <T>  type if the elements being checked
     * @return <code>true</code> iff all elements in <code>ts</code> pass
     * predicate function <code>check</code>
     */
    public static <T> boolean all(T[] ts, Predicate<T> check) {
        checkNotNull(ts, "Array must be provided");
        checkNotNull(check, "Predicate must be provided");

        for (T t : ts) {
            if (! check.test(t)) return false;
        }
        return true;
    }


    public static <T> boolean isEmpty(T[] arr) {
        if (arr == null) return true;
        return arr.length == 0;
    }


    public static int sum(int[] arr) {
        checkNotNull(arr, "arr cannot be null");
        int total = 0;
        for (int value : arr) {
            total += value;
        }
        return total;
    }

    public static <T> T last(T[] arr) {
        return arr[arr.length - 1];
    }


    public static <T> T[] initial(T[] bits) {
        return Arrays.copyOf(bits, bits.length - 1);
    }


    @SuppressWarnings("unchecked")
    public static <A, B> B[] map(A[] arr, Function<A, B> mapper) {
        return (B[]) Stream
                .of(arr)
                .map(mapper)
                .toArray();
    }

}
