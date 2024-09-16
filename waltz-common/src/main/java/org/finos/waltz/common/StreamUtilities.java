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


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;


public class StreamUtilities {

    public static <T> Collector<T, List<T>, Integer> batchProcessingCollector(int batchSize,
                                                                              Consumer<List<T>> batchProcessor) {
        return new BatchProcessingCollector<>(batchSize, batchProcessor);
    }


    @SafeVarargs
    public static <T> Stream<T> concat(Collection<T>... values) {
        return Stream.of(values)
                .flatMap(Collection::stream);
    }


    public static <T> Function<T, T> tap() {
        return tap(System.out::println);
    }


    public static <T> Function<T, T> tap(Consumer<T> consumer) {
        return t -> {
            consumer.accept(t);
            return t;
        };
    }


    public static <T> Stream<T> ofNullableArray(T[] arr) {
        return arr == null
                ? Stream.empty()
                : Stream.of(arr);
    }


    @SafeVarargs
    public static <T> Stream<T> concat(Stream<T>... streams) {
        return Stream
                .of(streams)
                .reduce(Stream.empty(),
                        Stream::concat);
    }


    public static Stream<String> lines(String multiLineStr) {
        String[] lines = multiLineStr.split("(\n|\r|\r\n)");
        return Stream.of(lines);
    }


    public static class Siphon<T> implements Predicate<T> {
        private final Predicate<T> pred;
        private final List<T> results = new ArrayList<>();

        Siphon(Predicate<T> pred) {
            this.pred = pred;
        }

        @Override
        public boolean test(T t) {
            boolean check = pred.test(t);
            if (check) results.add(t);
            return !check;
        }

        public List<T> getResults() {
            return results;
        }

        public Stream<T> stream() {
            return results.stream();
        }

        public boolean hasResults() {
            return !results.isEmpty();
        }

    }


    public static <T> Siphon<T> mkSiphon(Predicate<T> pred) {
        return new Siphon<T>(pred);
    }
}
