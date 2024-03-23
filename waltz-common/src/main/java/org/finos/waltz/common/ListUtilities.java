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

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.Checks.checkNotNull;


public class ListUtilities {

    /**
     * Construct an <code>ArrayList</code> from a vararg of elements
     *
     * @param ts  Array of elements to convert into list
     * @param <T> Type of each element
     * @return The resultant <code>ArrayList</code>
     */
    @SafeVarargs
    public static <T> ArrayList<T> newArrayList(T... ts) {
        ArrayList<T> list = new ArrayList<>(ts.length);
        Collections.addAll(list, ts);
        return list;
    }


    public static <T> List<T> append(List<T> ts, T t) {
        List<T> newList = new ArrayList<>(ts);
        newList.add(t);
        return newList;
    }


    @SafeVarargs
    public static <T> List<T> asList(T... ts){
        return newArrayList(ts);
    }


    /**
     * Given a collection of elements will return a list containing only non-null elements
     *
     * @param ts collection if items
     * @param <T> type of each item
     * @return list of non-null items
     */
    public static <T> List<T> compact(Collection<T> ts) {
        checkNotNull(ts, "Cannot compact a null list");
        return ts.stream()
                .filter(Objects::nonNull)
                .collect(toList());
    }


    @SafeVarargs
    public static <T> List<T> concat(List<? extends T>... tss) {
        List<T> result = new ArrayList<>();
        for (List<? extends T> ts : tss) {
            if (ts != null) {
                result.addAll(ts);
            }
        }
        return result;
    }


    public static <A, B> List<B> map(Collection<A> as, Function<A, B> mapper) {
        return as.stream()
                .map(mapper)
                .collect(toList());
    }


    public static <T> boolean isEmpty(List<T> ts) {
        return ts == null || ts.isEmpty();
    }


    public static <T> List<T> filter(Predicate<T> predicate, List<T> ts) {
        return ts.stream()
                .filter(predicate)
                .collect(toList());
    }


    public static <T> List<T> drop(List<T> ts, int count) {
        checkNotNull(ts, "list must not be null");
        return ts.stream()
                .skip(count)
                .collect(toList());
    }


    /**
     * @param ts collection of elements of type T.
     * @param <T> type of elements
     * @return <code>ts</code>  reversed, throws if <code>ts</code>  is null
     */
    public static <T> List<T> reverse(List<T> ts) {
        checkNotNull(ts, "list must not be null");
        ArrayList<T> cloned = new ArrayList<>(ts);
        Collections.reverse(cloned);
        return cloned;
    }


    /**
     * @param ts potentially null collection of T elements
     * @param <T> type of elements in the collection
     * @return  List representing <code>ts</code> or a new list if <code>ts</code> was null
     */
    public static <T> List<T> ensureNotNull(Collection<T> ts) {
        if (ts == null) {
            return newArrayList();
        } else {
            return new ArrayList<>(ts);
        }
    }


    @SafeVarargs
    public static <T> List<T> push(List<T> xs, T... elems) {
        return ListUtilities.concat(xs, Arrays.asList(elems));
    }


    /**
     * Apply mapFn to first element in list if found, otherwise return Optional.empty();
     * @param xs possible input elements
     * @param mapFn  function to transform an input element to desired output element
     * @param <X> type of input list
     * @param <Y> type of result
     * @return an optional result of applying the `mapFn` to the first element of `xs`
     */
    public static <X, Y> Optional<Y> applyToFirst(List<X> xs, Function<X, Y> mapFn) {
        if (isEmpty(xs)) return Optional.empty();

        return Optional.ofNullable(xs.get(0))
                .map(mapFn);
    }

    public static <X, Y> List<Tuple2<X, Y>> zip(List<X> xs, List<Y> ys) {
        return Seq.zip(xs, ys).collect(toList());
    }


    public static <T> boolean containsDuplicates(List<T> ts) {
        HashSet<T> seen = new HashSet<>();

        for (T t : ts) {
            boolean added = seen.add(t);
            if (!added) {
                return true;
            }
        }

        return false;
    }


    /**
     * Given a list and an index returns the element at that index wrapped in an Optional.
     * @param xs the list
     * @param idx the index to return
     * @return the element at the index wrapped in an Optional or Optional.empty() if the index is out of bounds
     * @param <T> type of elements in the list
     */
    public static <T> Optional<T> maybeGet(List<T> xs, int idx) {
        if (idx < 0 || idx >= xs.size()) {
            return Optional.empty();
        } else {
            return Optional.of(xs.get(idx));
        }
    }


    /**
     * Given a list, index and default value returns the element at that index or the default value if the index is out of bounds.
     */
    public static <T> T getOrDefault(List<T> xs, int idx, T defaultValue) {
        return maybeGet(xs, idx)
                .orElse(defaultValue);
    }


    /**
     * Returns a list of distinct values from a given list
     * @param ts  a list of T's with possible duplicates
     * @return  distinct values in <code>ts</code>
     * @param <T>
     */
    public static <T> List<T> distinct(List<T> ts) {
        return ts.stream().distinct().collect(toList());
    }
}
