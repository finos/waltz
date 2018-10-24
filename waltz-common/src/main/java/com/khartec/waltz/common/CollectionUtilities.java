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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.Checks.checkNotNull;


public class CollectionUtilities {

    /**
     * Returns an optional item representing the first thing in a collection to pass the given
     * predicate.  Since some collections are unordered first in this case means 'the first
     * item offered when streaming the collection'
     * @param pred
     * @param ts
     * @param <T>
     * @return
     */
    public static <T> Optional<T> find(Predicate<T> pred, Collection<T> ts) {
        checkNotNull(ts, "collection must not be null");
        checkNotNull(pred, "predicate cannot be null");

        return ts.stream()
                .filter(pred)
                .findFirst();
    }


    public static <T> boolean any(Collection<T> ts, Predicate<T> pred) {
        return find(pred, ts).isPresent();
    }


    /**
     * Returns the first item in a collections (as given by an iterator) or null if the
     * collection is empty.  A null collection
     * @param ts
     * @param <T>
     * @return
     */
    public static <T> T first(Collection<T> ts) {
        checkNotEmpty(ts, "Cannot get first item from an empty collection");

        return ts.iterator().next();
    }


    /**
     * convert the given collection into another using a transformation function
     * @param xs
     * @param fn
     * @param <X>
     * @param <Y>
     * @return
     */
    public static <X, Y> Collection<Y> map(Collection<X> xs, Function<X, Y> fn) {
        checkNotNull(xs, "collection must not be null");
        checkNotNull(fn, "transformation fn cannot be null");

        return xs.stream()
                .map(fn)
                .collect(Collectors.toList());
    }


    public static <X> Collection<X> filter(Collection<X> xs, Predicate<X> pred) {
        checkNotNull(xs, "collection must not be null");
        checkNotNull(pred, "predicate fn cannot be null");

        return xs.stream()
                .filter(pred)
                .collect(Collectors.toList());
    }

    public static <X> void maybe(Collection<X> xs, Consumer<Collection<X>> fn) {
        if (notEmpty(xs)) fn.accept(xs);
    }


    public static <X,Y> Y maybe(Collection<X> xs, Function<Collection<X>, Y> fn, Y dflt) {
        if (notEmpty(xs)) return fn.apply(xs);
        else return dflt;
    }


    public static  <T> boolean notEmpty(Collection<T> ts) {
        return ts != null && ! ts.isEmpty();
    }


    /**
     * Attempts to get the first element from <code>ts</code>.  Returns <code>Optional.empty()</code> if
     * the collection is null or empty.   The first element is derived by taking the first element offered up
     * by <code>ts.iterator()</code>
     * @param ts
     * @param <T>
     * @return
     */
    public static <T> Optional<T> head(Collection<T> ts) {
        return Optional.ofNullable(ts)
                .filter(x -> !x.isEmpty())
                .map(x -> first(x));
    }

    /**
     * Returns a sorted collection (list).  The input collection is unchanged.
     * @param xs
     * @param comparator
     * @param <X>
     * @return
     */
    public static <X> List<X> sort(Collection<X> xs, Comparator<? super X> comparator) {
        checkNotNull(xs, "xs cannot be null");
        checkNotNull(comparator, "comparator cannot be null");

        List<X> sorted = new ArrayList<X>(xs);
        sorted.sort(comparator);
        return sorted;
    }


    public static <X> X randomPick(Collection<X> xs) {
        checkNotNull(xs, "xs cannot be null");
        return ListUtilities.randomPick(new ArrayList<>(xs));
    }


    public static <X> boolean isEmpty(Collection<X> xs) {
        return xs == null || xs.isEmpty();
    }

}
