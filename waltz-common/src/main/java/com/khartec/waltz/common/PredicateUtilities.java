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

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;


public class PredicateUtilities {

    /**
     * Useful in a stream context. i.e.
     * <pre>
     *     .stream()
     *       .filter(not(Some::test))
     * </pre>
     * @param p  predicate to invert
     * @param <T>  type of object that the predicate will be testing
     * @return  true if the predicate fails, false if it passes
     */
    public static <T> Predicate<T> not(Predicate<T> p) {
        return p.negate();
    }


    /**
     * Test if all members of `ts` pass the predicate `p`.
     *
     * @param ts  Collection of elements to test
     * @param p  Predicate to test elements against
     * @param <T>  Type of the elements
     * @return  true iff all elements of `ts` pass `p`, or the collection is empty.
     */
    public static <T> boolean all(Collection<T> ts, Predicate<T> p) {
        return ts.stream()
                .allMatch(p);
    }


    /**
     * Test if any members of `ts` pass the predicate `p`.
     *
     * @param ts  Collection of elements to test
     * @param p  Predicate to test elements against
     * @param <T>  Type of the elements
     * @return  true if any elements of `ts` pass `p`, or the collection is empty.
     */
    public static <T> boolean any(Collection<T> ts, Predicate<T> p) {
        return ts.stream()
                .anyMatch(p);
    }


    /**
     * Test if _none_ of the members of `ts` pass the predicate `p`.
     *
     * @param ts  Collection of elements to test
     * @param p  Predicate to test elements against
     * @param <T>  Type of the elements
     * @return  true iff no elements in `ts` pass `p`, or the collection is empty.
     */
    public static <T> boolean none(Collection<T> ts, Predicate<T> p) {
        return ts.stream()
                .noneMatch(p);
    }


    public static <T> boolean all(T[] ts, Predicate<T> p) {
        return Stream.of(ts)
                .allMatch(p);
    }


    public static <T> boolean any(T[] ts, Predicate<T> p) {
        return Stream.of(ts)
                .anyMatch(p);
    }


    public static <T> boolean none(T[] ts, Predicate<T> p) {
        return Stream.of(ts)
                .noneMatch(p);
    }
}
