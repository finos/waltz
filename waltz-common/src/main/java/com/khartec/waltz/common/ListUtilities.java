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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;


public class ListUtilities {

    private static final Random rnd = new Random();


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
    public static <T> List<T> concat(List<T>... tss) {
        List<T> result = new ArrayList<>();
        for (List<T> ts : tss) {
            if (ts != null) {
                result.addAll(ts);
            }
        }
        return result;
    }


    public static <A, B> List<B> map(List<A> as, Function<A, B> mapper) {
        return as.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }


    public static <T> boolean isEmpty(List<T> ts) {
        return ts == null || ts.isEmpty();
    }


    public static <T> T randomPick(List<T> ts) {
        return ts.get(rnd.nextInt(ts.size()));
    }


    public static <T> List<T> filter(Predicate<T> predicate, List<T> ts) {
        return ts.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }


    public static <T> List<T> drop(List<T> ts, int count) {
        checkNotNull(ts, "list must not be null");
        return ts.stream()
                .skip(count)
                .collect(Collectors.toList());
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


    public static <T> Builder<T> builder(Class<T> cls) {
        return new Builder<>();
    }


    public static class Builder<T> {

        private List<T> workingList = new LinkedList<>();

        @SafeVarargs
        public final Builder<T> addAll(T... ts) {
            workingList.addAll(Arrays.asList(ts));
            return this;
        }

        public final Builder<T> addAll(List<T> ts) {
            workingList.addAll(ts);
            return this;
        }

        public List<T> build() {
            return Collections.unmodifiableList(workingList);
        }
    }


    /**
     * Apply mapFn to first element in list if found, otherwise return Optional.empty();
     * @param xs possible input elements
     * @param mapFn  function to transform an input element to desired output element
     * @param <X> type of input list
     * @param <Y> type of result
     * @return
     */
    public static <X, Y> Optional<Y> applyToFirst(List<X> xs, Function<X, Y> mapFn) {
        if (isEmpty(xs)) return Optional.empty();

        return Optional.ofNullable(xs.get(0))
                .map(mapFn);
    }
}
