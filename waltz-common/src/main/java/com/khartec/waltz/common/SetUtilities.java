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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;


public class SetUtilities {

    public static <T> Set<T> fromArray(T... ts) {
        if (ts == null || ts.length == 0) return new HashSet<>();

        return new HashSet<>(Arrays.asList(ts));
    }

    public static <X, Y> Set<Y> map(Collection<X> xs, Function<X, Y> fn) {
        if (xs == null || xs.isEmpty()) return new HashSet<>();
        return xs.stream()
                .map(fn)
                .collect(Collectors.toSet());
    }

    public static <T> Set<T> union(Collection<T>... xss) {
        Set<T> result = new HashSet<>();
        for (Collection xs : xss) {
            result.addAll(xs);
        }
        return result;
    }
}
