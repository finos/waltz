package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BatchProcessingCollector_accumulatorTest {

    @Test
    public void canGetAccumulator() {
        List<String> elements = ListUtilities.newArrayList("a", "b");
        Consumer display = a -> System.out.println("Consumed: "+a);
        display.accept(elements);
        BatchProcessingCollector batch = new BatchProcessingCollector(10, display);
        BiConsumer bic = batch.accumulator();
        assertNotNull(bic);
    }

    @Test
    public void canGetAccumulatorForNullConsumer() {
        BatchProcessingCollector batch = new BatchProcessingCollector(10, null);
        BiConsumer bic = batch.accumulator();
        assertNotNull(bic);
    }

    @Test
    public void canGetAccumulatorForZeroBatchSize() {
        List<String> elements = ListUtilities.newArrayList("a", "b");
        Consumer display = a -> System.out.println("Consumed: "+a);
        display.accept(elements);
        BatchProcessingCollector batch = new BatchProcessingCollector(0, display);
        BiConsumer bic = batch.accumulator();
        assertNotNull(bic);
    }

    @Test
    public void canGetAccumulatorForNullConsumerAndZeroBatchSize() {
        BatchProcessingCollector batch = new BatchProcessingCollector(0, null);
        BiConsumer bic = batch.accumulator();
        assertNotNull(bic);
    }
}
