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


    public static <T> Stream<T> concat(Collection<T>... values) {
        return Stream.of(values)
                .flatMap(Collection::stream);
    }

    public static <T> Function<T, T> tap() {
        return t -> {
            System.out.println(t);
            return t;
        };
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


    public static <T> Stream<T> concat(Stream<T>... streams) {
        return Stream.of(streams)
                .reduce(Stream.empty(),
                        (acc, s) -> Stream.concat(acc, s));
    }


    public static class Siphon<T> implements Predicate<T> {
        private final Predicate<T> pred;
        private final List<T> results = new ArrayList();

        public Siphon(Predicate<T> pred) {
            this.pred = pred;
        }

        @Override
        public boolean test(T t) {
            boolean check = pred.test(t);
            if (check) results.add(t);
            return ! check;
        }

        public List<T> getResults() {
            return results;
        }
    }

    public static <T> Siphon<T> mkSiphon(Predicate<T> pred) {
        return new Siphon(pred);
    }
}
