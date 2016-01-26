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
import java.util.stream.Collectors;


public class MapUtilities {

    public static <K, V> HashMap<K, V> newHashMap() {
         return new HashMap<>();
    }

    public static <K, V> HashMap<K, V> newHashMap(K key, V val) {
        HashMap<K, V> map = newHashMap();
        map.put(key, val);
        return map;
    }


    public static <K, V> Map<K, V> newHashMap(K k1, V v1, K k2, V v2) {
        HashMap<K, V> map = newHashMap(k1, v1);
        map.put(k2, v2);
        return map;
    }


    public static <K, V> Map<K, Collection<V>> groupBy(Function<V, K> keyFn, Collection<V> xs) {
        return groupBy(keyFn, x -> x, xs);
    }

    public static <K, V, V2> Map<K, Collection<V2>> groupBy(Function<V, K> keyFn,
                                                       Function<V, V2> valueFn,
                                                       Collection<V> xs) {
        if (xs == null) return Collections.emptyMap();
        Map<K, Collection<V2>> result = MapUtilities.newHashMap();
        for (V v: xs) {
            K key = keyFn.apply(v);
            Collection<V2> bucket = result.computeIfAbsent(key, u -> ListUtilities.newArrayList());
            bucket.add(valueFn.apply(v));
            result.put(key, bucket);
        }
        return result;
    }

    public static <K, V> Map<K, V> indexBy(Function<V, K> keyFn, Collection<V> xs) {
        Map<K, V> result = MapUtilities.newHashMap();
        for (V v: xs) {
            K key = keyFn.apply(v);
            result.put(key, v);
        }
        return result;
    }

    public static <K, V> Map<K, Long> countBy(Function<V, K> keyFn, Collection<V> xs) {
        if (xs == null) {
            return Collections.emptyMap();
        }
        return xs.stream()
                .collect(Collectors.groupingBy(keyFn, Collectors.counting()));
    }


    public static <K, V> Map<K, V> ensureNotNull(Map<K, V> maybeMap) {
        return maybeMap == null ? newHashMap() : maybeMap;
    }


    public static <K, V> boolean isEmpty(Map<K, V> map) {
        return map == null || map.isEmpty();
    }


    public static <K, V> Optional<V> maybeGet(Map<K, V> map, K key) {
        if (map == null) { return Optional.empty(); }
        return Optional.ofNullable(map.get(key));
    }
}
