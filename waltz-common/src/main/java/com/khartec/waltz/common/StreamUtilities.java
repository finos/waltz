package com.khartec.waltz.common;


import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
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
}
