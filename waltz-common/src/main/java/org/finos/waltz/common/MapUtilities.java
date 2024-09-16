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

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.finos.waltz.common.Checks.checkNotNull;
import static java.util.Collections.emptyMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;


public class MapUtilities {

    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<>();
    }


    public static <K, V> Map<K, V> newHashMap(K key, V val) {
        HashMap<K, V> map = newHashMap();
        map.put(key, val);
        return map;
    }


    public static <K, V> Map<K, V> newHashMap(K k1, V v1,
                                              K k2, V v2) {
        Map<K, V> map = newHashMap(k1, v1);
        map.put(k2, v2);
        return map;
    }


    public static <K, V> Map<K, V> newHashMap(K k1, V v1,
                                              K k2, V v2,
                                              K k3, V v3) {
        Map<K, V> map = newHashMap(k1, v1, k2, v2);
        map.put(k3, v3);
        return map;
    }


    public static <K, V> Map<K, V> newHashMap(K k1, V v1,
                                              K k2, V v2,
                                              K k3, V v3,
                                              K k4, V v4) {
        Map<K, V> map = newHashMap(k1, v1, k2, v2, k3, v3);
        map.put(k4, v4);
        return map;
    }


    public static <K, V> Map<K, V> newHashMap(K k1, V v1,
                                              K k2, V v2,
                                              K k3, V v3,
                                              K k4, V v4,
                                              K k5, V v5) {
        Map<K, V> map = newHashMap(k1, v1, k2, v2, k3, v3, k4, v4);
        map.put(k5, v5);
        return map;
    }


    public static <K, V> Map<K, V> newHashMap(K k1, V v1,
                                              K k2, V v2,
                                              K k3, V v3,
                                              K k4, V v4,
                                              K k5, V v5,
                                              K k6, V v6) {
        Map<K, V> map = newHashMap(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
        map.put(k6, v6);
        return map;
    }


    public static <K, V> Map<K, Collection<V>> groupBy(Function<V, K> keyFn,
                                                       Collection<V> xs) {
        return groupBy(keyFn, x -> x, xs);
    }

    public static <K, V> Map<K, Collection<V>> groupBy(Collection<V> xs,
                                                       Function<V, K> keyFn) {
        return groupBy(keyFn, x -> x, xs);
    }


    public static <K, V, V2> Map<K, Collection<V2>> groupBy(Collection<V> xs,
                                                            Function<V, K> keyFn,
                                                            Function<V, V2> valueFn) {
        return groupBy(keyFn, valueFn, xs);
    }

    public static <K, V, V2> Map<K, Collection<V2>> groupBy(Function<V, K> keyFn,
                                                            Function<V, V2> valueFn,
                                                            Collection<V> xs) {
        checkNotNull(xs, "xs cannot be null");
        checkNotNull(keyFn, "keyFn cannot be null");
        checkNotNull(valueFn, "valueFn cannot be null");

        Map<K, Collection<V2>> result = MapUtilities.newHashMap();
        for (V v: xs) {
            K key = keyFn.apply(v);
            Collection<V2> bucket = result.computeIfAbsent(key, u -> ListUtilities.newArrayList());
            bucket.add(valueFn.apply(v));
            result.put(key, bucket);
        }
        return result;
    }


    public static <K, V, V2> TreeMap<K, Collection<V2>> orderedGroupBy(Collection<V> xs,
                                                                       Function<V, K> keyFn,
                                                                       Function<V, V2> valueFn,
                                                                       Comparator<K> comparator) {
        checkNotNull(xs, "xs cannot be null");
        checkNotNull(keyFn, "keyFn cannot be null");
        checkNotNull(valueFn, "valueFn cannot be null");

        TreeMap<K, Collection<V2>> result = new TreeMap<>(comparator);

        for (V v: xs) {
            K key = keyFn.apply(v);
            Collection<V2> bucket = result.computeIfAbsent(key, u -> ListUtilities.newArrayList());
            bucket.add(valueFn.apply(v));
            result.put(key, bucket);
        }
        return result;
    }


    public static <K, V> Map<K, V> indexBy(Collection<V> xs,
                                           Function<V, K> keyFn) {
        return indexBy(keyFn, xs);
    }


    public static <K, V, V2> Map<K, V2> indexBy(Collection<V> xs,
                                                Function<V, K> keyFn,
                                                Function<V, V2> valueFn) {
        return indexBy(keyFn, valueFn, xs);
    }


    public static <K, V> Map<K, V> indexBy(Function<V, K> keyFn,
                                           Collection<V> xs) {
        checkNotNull(xs, "xs cannot be null");
        checkNotNull(keyFn, "keyFn cannot be null");

        return indexBy(keyFn, identity(), xs);
    }


    public static <K, R, V> Map<K, R> indexBy(Function<V, K> keyFn,
                                              Function<V, R> valueFn,
                                              Collection<V> xs) {
        checkNotNull(xs, "xs cannot be null");
        checkNotNull(keyFn, "keyFn cannot be null");
        checkNotNull(valueFn, "valueFn cannot be null");

        return xs
                .stream()
                .collect(
                        HashMap::new,
                        (acc, d) -> acc.put(
                                keyFn.apply(d),
                                valueFn.apply(d)),
                        Map::putAll);
    }


    public static <K, R, V> Map<K, R> indexBy(Function<V, K> keyFn,
                                              Function<V, R> valueFn,
                                              Collection<V> xs,
                                              BinaryOperator<R> mergeFunction) {
        checkNotNull(xs, "xs cannot be null");
        checkNotNull(keyFn, "keyFn cannot be null");
        checkNotNull(valueFn, "valueFn cannot be null");

        return xs.stream()
                .collect(toMap(keyFn, valueFn, mergeFunction));
    }


    public static <K, R, V> Map<K, R> indexBy(Collection<V> xs,
                                              Function<V, K> keyFn,
                                              Function<V, R> valueFn,
                                              BinaryOperator<R> mergeFunction) {
        return indexBy(keyFn, valueFn, xs, mergeFunction);
    }


    public static <K, V> Map<K, Long> countBy(Function<V, K> keyFn,
                                              Collection<V> xs) {
        return countBy(xs, keyFn);
    }

    public static <K, V> Map<K, Long> countBy(Collection<V> xs,
                                              Function<V, K> keyFn) {
        if (xs == null) {
            return emptyMap();
        }
        return xs.stream()
                .collect(Collectors.groupingBy(keyFn, Collectors.counting()));
    }


    public static <K, V> Map<K, V> ensureNotNull(Map<K, V> maybeMap) {
        return maybeMap == null
                ? newHashMap()
                : maybeMap;
    }


    /**
     * Returns true if map is null or empty
     * @param map  the map to test
     * @param <K>  type of the keys in the map
     * @param <V>  type of the values in the map
     * @return  true if the map is null or empty
     */
    public static <K, V> boolean isEmpty(Map<K, V> map) {
        return map == null || map.isEmpty();
    }


    public static <K, V> Optional<V> maybeGet(Map<K, V> map,
                                              K key) {
        return map == null
                ? Optional.empty()
                : Optional.ofNullable(map.get(key));
    }


    /**
     * Similar to groupBy, however the valueFn runs over the entire group after the initial grouping
     * has been performed
     * @param xs - initial values
     * @param keyFn  - extracts/derives the grouping key
     * @param valueFn - function which transforms each group
     * @param <K> - key type
     * @param <V> - (initial value type)
     * @param <V2> - resultant value type
     * @return a new map where elements of xs have been grouped by the key fn,
     *      the resultant group is then transformed using the valueFn.
     */
    public static <K, V, V2> Map<K, V2> groupAndThen(Collection<V> xs,
                                                     Function<V, K> keyFn,
                                                     Function<Collection<V>, V2> valueFn) {
        checkNotNull(xs, "xs cannot be null");
        checkNotNull(keyFn, "keyFn cannot be null");
        checkNotNull(valueFn, "valueFn cannot be null");

        Map<K, V2> result = MapUtilities.newHashMap();
        Map<K, Collection<V>> step1 = groupBy(keyFn, xs);

        for (Map.Entry<K, Collection<V>> entry : step1.entrySet()) {

            K key = entry.getKey();
            Collection<V> group = entry.getValue();
            V2 transformedGroup = valueFn.apply(group);
            result.put(key, transformedGroup);
        }
        return result;
    }


    public static<K, K2, V> Map<K2, V> transformKeys(Map<K, V> original, Function<K, K2> transformation) {
        Map<K2, V> output = new HashMap<>();
        original.forEach((key, value) -> output.put(transformation.apply(key), value));
        return output;
    }


    /**
     * Given two maps where `map1` (provides the 'domain') goes from K1 -> K2 and
     * `map2` (provides the 'range') goes from K2 -> V returns a new map which
     * joins map1 and map2 giving a result which goes directly
     * from K1 -> V
     * @param map1 the domain map
     * @param map2 the range map
     * @param <K1> the key of the domain
     * @param <K2> the value of the domain and the key of the range map
     * @param <V> the value of the range map
     * @return a map going from K1 -> V
     */
    public static <K1, K2, V> Map<K1, V> compose(Map<K1, K2> map1, Map<K2, V> map2) {
        Map<K1, V> result = new HashMap<>();
        map1.forEach((key, value) -> result.put(key, map2.get(value)));
        return result;
    }

    /**
     * @param maps list of maps to merge, later maps override where key is shared with earlier map
     * @param <K>  Key of Map
     * @param <V>  Value of Map
     * @return new merged Map<K, V>
     */
    public static <K, V> Map<K, V> merge(Map<K, V>... maps) {
        HashMap<K, V> newMap = newHashMap();
        Stream.of(maps)
                .forEach(newMap::putAll);

        return newMap;
    }
}
