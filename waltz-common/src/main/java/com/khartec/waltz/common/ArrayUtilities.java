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

import java.util.Arrays;
import java.util.Random;
import java.util.function.Predicate;

import static com.khartec.waltz.common.Checks.checkNotNull;

public class ArrayUtilities {


    private static final Random rnd = new Random();


    /**
     * @throws IllegalArgumentException If either <code>ts</code>
     * or <code>check</code> is null
     * @param ts - array of elements of type <code>T</code>
     * @param check - predicate function
     * @param <T>
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


    public static <T> T randomPick(T... ts) {
        Checks.checkNotEmpty(ts, "Cannot take random pick from an empty array");
        int idx = rnd.nextInt(ts.length);
        return ts[idx];
    }


    public static <T> boolean isEmpty(T[] arr) {
        if (arr == null) return true;
        if (arr.length == 0) return true;
        return false;
    }


    public static int sum(int[] arr) {
        checkNotNull(arr, "arr cannot be null");
        int total = 0;
        for (int i = 0; i < arr.length; i++) {
            total += arr[i];
        }
        return total;
    }

    public static <T> T last(T[] arr) {
        return arr[arr.length - 1];
    }

    public static <T> T[] initial(T[] bits) {
        return Arrays.copyOf(bits, bits.length - 1);
    }
}
