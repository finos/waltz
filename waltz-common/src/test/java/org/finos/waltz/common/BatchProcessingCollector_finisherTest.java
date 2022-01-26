package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertNotNull;


public class BatchProcessingCollector_finisherTest {
    @Test
    public void canGetFinisher() {
        List<String> elements = ListUtilities.newArrayList("a", "b");
        Consumer display = a -> System.out.println("Consumed: "+a);
        display.accept(elements);
        BatchProcessingCollector batch = new BatchProcessingCollector(10, display);
        Function fun = batch.finisher();
        assertNotNull(fun);
    }

    @Test
    public void canGetFinisherForNullConsumer() {
        BatchProcessingCollector batch = new BatchProcessingCollector(10, null);
        Function fun = batch.finisher();
        assertNotNull(fun);
    }

    @Test
    public void canGetFinisherForZeroBatchSize() {
        List<String> elements = ListUtilities.newArrayList("a", "b");
        Consumer display = a -> System.out.println("Consumed: "+a);
        display.accept(elements);
        BatchProcessingCollector batch = new BatchProcessingCollector(0, display);
        Function fun = batch.finisher();
        assertNotNull(fun);
    }

    @Test
    public void canGetFinisherForNullConsumerAndZeroBatchSize() {
        BatchProcessingCollector batch = new BatchProcessingCollector(0, null);
        Function fun = batch.finisher();
        assertNotNull(fun);
    }
}
