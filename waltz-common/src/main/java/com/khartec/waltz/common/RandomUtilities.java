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

package com.khartec.waltz.common;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.CollectionUtilities.isEmpty;

public class RandomUtilities {

    private static Random rnd = new Random();


    public static Random getRandom() {
        return rnd;
    }


    public static int randomIntBetween(int lower, int upper) {
        return lower + rnd.nextInt(upper - lower);
    }


    /**
     * Given a collection of items this will randomly pick an item.
     * The random choice is via `java.util.Random` with the default
     * constructor.
     *
     * @param xs  items to pick from
     * @param <X> type of items in collection
     * @return an item
     * @throws IllegalArgumentException if the collection is empty (or null)
     */
    public static <X> X randomPick(Collection<X> xs) {
        checkNotEmpty(xs, "xs cannot be null");
        List<X> asList = xs instanceof List
                ? (List<X>) xs
                : new ArrayList<>(xs);
        return randomPick(asList);
    }


    public static <T> T randomPick(List<T> ts) {
        return ts.get(rnd.nextInt(ts.size()));
    }


    public static <T> List<T> randomPick(Collection<T> choices, int howMany) {
        if (isEmpty(choices) || howMany <= 0) {
            return Collections.emptyList();
        }

        List<T> shuffled = new ArrayList<>(choices);
        Collections.shuffle(shuffled);

        if (howMany > shuffled.size()) {
            return shuffled;
        }

        return shuffled
                .stream()
                .limit(howMany)
                .collect(Collectors.toList());
    }


    @SafeVarargs
    public static <T> T randomPick(T... ts) {
        Checks.checkNotEmpty(ts, "Cannot take random pick from an empty array");
        int idx = rnd.nextInt(ts.length);
        return ts[idx];
    }


    public static IntStream randomlySizedIntStream(int lower, int upper) {
        return IntStream.range(0, randomIntBetween(lower, upper));
    }

}
