/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;

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
        Checks.checkNotNull(ts, "Array must be provided");
        Checks.checkNotNull(check, "Predicate must be provided");

        for (T t : ts) {
            if (! check.test(t)) return false;
        }
        return true;

    }


    public static <X, Y> List<Y> mapToList(X[] xs, Function<X, Y> transformer) {
        Checks.checkNotNull(xs, "array must not be null");
        Checks.checkNotNull(transformer, "transformer must not be null");

        LinkedList<Y> ys = new LinkedList<>();

        for (X x : xs) {
            ys.add(transformer.apply(x));
        }
        return ys;
    }


    public static <T> T randomPick(T... ts) {
        Checks.checkNotEmpty(ts, "Cannot take random pick from an empty array");
        int idx = rnd.nextInt(ts.length);
        return ts[idx];
    }




    public static <T> T[] of(T[] ts, T... moreTs) {
        List<T> init = ListUtilities.newArrayList(ts);
        List<T> rest = ListUtilities.newArrayList(moreTs);

        List<T> res = new ArrayList<>(ts.length + moreTs.length);
        res.addAll(init);
        res.addAll(rest);

        return (T[]) res.toArray();
    }


    public static <T> boolean isEmpty(T[] arr) {
        if (arr == null) return true;
        if (arr.length == 0) return true;
        return false;
    }
}
