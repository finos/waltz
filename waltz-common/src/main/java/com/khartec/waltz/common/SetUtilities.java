/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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

import static com.khartec.waltz.common.Checks.checkNotNull;
import static java.util.stream.Collectors.toSet;


public class SetUtilities {

    public static <T> Set<T> asSet(T... ts) {
        return fromArray(ts);
    }


    public static <T> Set<T> fromArray(T... ts) {
        if (ts == null || ts.length == 0) return new HashSet<>();

        return new HashSet<>(Arrays.asList(ts));
    }

    public static <T> Set<T> fromCollection(Collection<T> ts) {
        if (ts == null || ts.isEmpty()) return new HashSet<>();

        return new HashSet<>(ts);
    }

    public static <X, Y> Set<Y> map(Collection<X> xs, Function<X, Y> fn) {
        if (xs == null || xs.isEmpty()) return new HashSet<>();
        return xs.stream()
                .map(fn)
                .collect(toSet());
    }

    @SafeVarargs
    public static <T> Set<T> union(Collection<T>... xss) {
        Set<T> result = new HashSet<>();
        for (Collection<T> xs : xss) {
            result.addAll(xs);
        }
        return result;
    }

    @SafeVarargs
    public static <T> Set<T> orderedUnion(Collection<T>... xss) {
        // LinkedHashSet preserves iteration ordering, source: https://stackoverflow.com/a/16480560
        Set<T> result = new LinkedHashSet<>();
        for (Collection<T> xs : xss) {
            result.addAll(xs);
        }

        return result;
    }


    /**
     * Remove vararg of <code>ys's</code> from <code>xs</code>
     * @param xs
     * @param yss
     * @param <T>
     * @return xs without all members of yss
     */
    @SafeVarargs
    public static <T> Set<T> minus(Set<T> xs, Set<T>... yss) {
        checkNotNull(xs, "xs cannot be null");
        checkNotNull(yss, "yss cannot be null");

        Set<T> working = new HashSet<>(xs);
        ArrayList<Set<T>> sets = ListUtilities.newArrayList(yss);
        sets.forEach(working::removeAll);

        return working;
    }


    public static <T> Set<T> intersection(Set<T> xs, Set<T> ys) {
        checkNotNull(xs, "xs cannot be null");
        checkNotNull(ys, "ys cannot be null");

        Set<T> working = new HashSet<>(xs);
        working.retainAll(ys);
        return working;
    }


    public static <T, K> Set<T> uniqBy(Collection<T> xs, Function<T, K> comparator) {
        Set<K> seen = new HashSet<>();
        return xs.stream()
                .filter(x -> {
                    K k = comparator.apply(x);
                    if (seen.contains(k)) {
                        return false;
                    } else {
                        seen.add(k);
                        return true;
                    }
                })
                .collect(toSet());
    }


    public static <T> Collection<T> unionAll(Collection<? extends Collection<T>> values) {
        checkNotNull(values, "Cannot union all a null collection of collections");
        Set<T> result = new HashSet<>();
        values.forEach(result::addAll);
        return result;
    }
}
