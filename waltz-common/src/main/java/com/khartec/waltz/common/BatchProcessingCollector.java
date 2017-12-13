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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;
import java.util.stream.Collector;


public class BatchProcessingCollector<T> implements Collector<T, List<T>, Integer> {

    private final int batchSize;
    private final Consumer<List<T>> batchProcessor;
    private final AtomicInteger processedCount = new AtomicInteger(0);

    public BatchProcessingCollector(int batchSize,
                                    Consumer<List<T>> batchProcessor) {
        this.batchSize = batchSize;
        this.batchProcessor = list -> {
                batchProcessor.accept(list);
                processedCount.addAndGet(list.size());
        };
    }

    @Override
    public Supplier<List<T>> supplier() {
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<T>, T> accumulator() {
        return (list, value) -> {
            list.add(value);
            if (list.size() >= batchSize) {
                batchProcessor.accept(list);
                list.clear();
            }
        };
    }

    @Override
    public BinaryOperator<List<T>> combiner() {
        return (list1, list2) -> {
            batchProcessor.accept(list1);
            batchProcessor.accept(list2);
            return new ArrayList<>();
        };
    }

    @Override
    public Function<List<T>, Integer> finisher() {
        return list -> {
            batchProcessor.accept(list);
            return processedCount.get();
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }
}
