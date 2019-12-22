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
