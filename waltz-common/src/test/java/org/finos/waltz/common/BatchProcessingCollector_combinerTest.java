package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BatchProcessingCollector_combinerTest {
    @Test
    public void canGetCombiner() {
        List<String> elements = ListUtilities.newArrayList("a", "b");
        Consumer display = a -> System.out.println("Consumed: "+a);
        display.accept(elements);
        BatchProcessingCollector batch = new BatchProcessingCollector(10, display);
        BinaryOperator bin = batch.combiner();
        assertNotNull(bin);
    }

    @Test
    public void canGetCombinerForNullConsumer() {
        BatchProcessingCollector batch = new BatchProcessingCollector(10, null);
        BinaryOperator bin = batch.combiner();
        assertNotNull(bin);
    }

    @Test
    public void canGetCombinerForZeroBatchSize() {
        List<String> elements = ListUtilities.newArrayList("a", "b");
        Consumer display = a -> System.out.println("Consumed: "+a);
        display.accept(elements);
        BatchProcessingCollector batch = new BatchProcessingCollector(0, display);
        BinaryOperator bin = batch.combiner();
        assertNotNull(bin);
    }

    @Test
    public void canGetCombinerForNullConsumerAndZeroBatchSize() {
        BatchProcessingCollector batch = new BatchProcessingCollector(0, null);
        BinaryOperator bin = batch.combiner();
        assertNotNull(bin);
    }
}
