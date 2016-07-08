/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.common;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.Checks.checkNotNull;


public class CollectionUtilities {

    public static <T> Optional<T> find(Predicate<T> pred, Collection<T> ts) {
        checkNotNull(ts, "collection must not be null");
        checkNotNull(pred, "predicate cannot be null");

        return ts.stream()
                .filter(pred)
                .findFirst();
    }


    public static <T> T first(Collection<T> ts) {
        checkNotEmpty(ts, "Cannot get first item from an empty collection");

        return ts.iterator().next();
    }


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
}
