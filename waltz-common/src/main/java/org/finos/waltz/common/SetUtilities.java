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

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.finos.waltz.common.Checks.checkNotNull;
import static java.util.stream.Collectors.toSet;


public class SetUtilities {

    @SafeVarargs
    public static <T> Set<T> asSet(T... ts) {
        return fromArray(ts);
    }


    @SafeVarargs
    public static <T> Set<T> fromArray(T... ts) {
        if (ts == null || ts.length == 0) return new HashSet<>();

        return new HashSet<>(Arrays.asList(ts));
    }

    public static <T> Set<T> fromCollection(Collection<? extends T> ts) {
        if (ts == null || ts.isEmpty()) return new HashSet<>();

        return new HashSet<>(ts);
    }

    public static <X, Y> Set<Y> map(Collection<X> xs, Function<X, Y> fn) {
        if (xs == null || xs.isEmpty()) return new HashSet<>();
        return xs.stream()
                .map(fn)
                .collect(toSet());
    }

    public static <X> Set<X> filter(Collection<X> xs, Predicate<X> predicate) {
        if (xs == null || xs.isEmpty()) return new HashSet<>();
        return xs.stream()
                .filter(predicate)
                .collect(toSet());
    }

    @SafeVarargs
    public static <T> Set<T> union(Collection<? extends T>... xss) {
        Set<T> result = new HashSet<>();
        for (Collection<? extends T> xs : xss) {
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
     * @param xs the set to subtract from
     * @param yss var of of sets to remove from `xs`
     * @param <T>  type of the elements in the sets
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

    public static <T> Set<T> complement(Set<T> xs, Set<T> ys) {
        checkNotNull(xs, "xs cannot be null");
        checkNotNull(ys, "ys cannot be null");

        return minus(union(xs,ys), intersection(xs,ys));

    }


    public static <T> boolean hasIntersection(Set<T> xs,
                                              Set<T> ys) {
        return ! intersection(xs, ys).isEmpty();
    }


    public static <T> Set<T> fromOptionals(Collection<Optional<T>> ts) {
        return ts
                .stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

}
