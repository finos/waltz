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
     *
     * @param pred predicate to determine if an item satisfies the 'find' criteria
     * @param ts collection of items
     * @param <T> type of the items
     * @return Optional of the first found item or an empty optional if not found
     */
    public static <T> Optional<T> find(Predicate<T> pred, Collection<T> ts) {
        checkNotNull(ts, "collection must not be null");
        checkNotNull(pred, "predicate cannot be null");

        return ts.stream()
                .filter(pred)
                .findFirst();
    }


    /**
     * given a collection and a predicate will (eagerly) evaluate the items in the
     * collection to see if any satisfy the predicate.
     *
     * @param ts collection of items
     * @param pred predicate to test if an item in the collection satisfies the 'find' condition
     * @param <T> type of the items
     * @return true if any item in the collection satisfies the given predicate
     */
    public static <T> boolean any(Collection<T> ts, Predicate<T> pred) {
        return find(pred, ts).isPresent();
    }


    /**
     * Returns the first item in a collections (as given by an iterator) or null if the
     * collection is empty.  A null collection will throw an exception.
     *
     * @param ts collection of items
     * @param <T> type of the items
     * @return the first element or an exception if the collection is empty
     */
    public static <T> T first(Collection<T> ts) {
        checkNotEmpty(ts, "Cannot get first item from an empty collection");

        return ts.iterator().next();
    }


    /**
     * convert the given collection into another using a transformation function
     * @param xs starting collection
     * @param fn function to transform elements of the starting collection
     * @param <X> type of starting collection
     * @param <Y> type of resultant collection
     * @return new collection containing elements of the starting collection transformed by the given function
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


    /**
     * If the given collection is not empty then run a function over the entire collection
     *
     * @param xs collection of items
     * @param fn consumer function which accepts the items
     * @param <X> type of the items
     */
    public static <X> void maybe(Collection<X> xs, Consumer<Collection<X>> fn) {
        if (notEmpty(xs)) fn.accept(xs);
    }


    /**
     * If the given collection (`xs`) is not empty apply the given function to it (`fn(xs)`)
     * and return the result.  If the collection is null then return the supplied default
     * argument.
     *
     * @param xs - collection
     * @param fn - transformation for the collection
     * @param dflt  default value to return if the collection is empty
     * @param <X> type of the items in the collection
     * @param <Y> resultant type of the transformation
     * @return the result of `fn(xs)` or `dflt` if xs is empty
     */
    public static <X,Y> Y maybe(Collection<X> xs, Function<Collection<X>, Y> fn, Y dflt) {
        if (notEmpty(xs)) return fn.apply(xs);
        else return dflt;
    }


    /**
     * `notEmpty` is intended as a null-safe way to determine if a collection
     * is not empty.  Null and empty collections are both treated as empty.
     *
     * @param ts collection or null
     * @param <T> type of items in collection
     * @return true if the collection is not null and not empty
     */
    public static  <T> boolean notEmpty(Collection<T> ts) {
        return ts != null && ! ts.isEmpty();
    }


    /**
     * Attempts to get the first element from <code>ts</code>.  Returns <code>Optional.empty()</code> if
     * the collection is null or empty.   The first element is derived by taking the first element offered up
     * by <code>ts.iterator()</code>
     *
     * @param xs
     * @param <X>
     * @return
     */
    public static <X> Optional<X> head(Collection<X> xs) {
        return isEmpty(xs)
                ? Optional.empty()
                : Optional.of(first(xs));
    }


    /**
     * Returns a sorted collection (list).  The input collection is unchanged.
     * @param xs collection to be sorted (will be unchanged)
     * @param comparator used to determine order
     * @param <X> type of elements in `xs
     * @return new list with members of `xs` sorted by `comparator`.
     */
    public static <X> List<X> sort(Collection<X> xs, Comparator<? super X> comparator) {
        checkNotNull(xs, "xs cannot be null");
        checkNotNull(comparator, "comparator cannot be null");

        List<X> sorted = new ArrayList<X>(xs);
        sorted.sort(comparator);
        return sorted;
    }


    /**
     * Given a collection of items this will randomly pick an item.
     * The random choice is via `java.util.Random` with the default
     * constructor.
     *
     * @param xs items to pick from
     * @param <X> type of items in collection
     * @return an item
     * @throws IllegalArgumentException if the collection is empty (or null)
     */
    public static <X> X randomPick(Collection<X> xs) {
        checkNotEmpty(xs, "xs cannot be null");
        List<X> asList = xs instanceof List
                ? (List<X>) xs
                : new ArrayList<X>(xs);
        return ListUtilities.randomPick(asList);
    }


    /**
     * Intended as a null-safe test for empty collections.
     *
     * @param xs collection of items (or null)
     * @param <X> type of items in collection
     * @return true if the collection is empty or null
     */
    public static <X> boolean isEmpty(Collection<X> xs) {
        return xs == null || xs.isEmpty();
    }

}
