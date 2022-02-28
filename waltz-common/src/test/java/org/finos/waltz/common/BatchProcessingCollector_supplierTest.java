package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class BatchProcessingCollector_supplierTest {
    @Test
    public void canGetSupplier() throws Exception {
        List<String> elements = ListUtilities.newArrayList("a", "b");
        Consumer display = a -> System.out.println("Consumed: " + a);
        display.accept(elements);
        BatchProcessingCollector batch = new BatchProcessingCollector(10, display);
        Supplier<List<Throwable>> sup = batch.supplier();
        assertTrue(sup.get().isEmpty());
    }

    @Test
    public void canGetSupplierForNullConsumer() throws Exception {
        BatchProcessingCollector batch = new BatchProcessingCollector(10, null);
        Supplier<List<Throwable>> sup = batch.supplier();
        assertTrue(sup.get().isEmpty());
    }

    @Test
    public void canGetSupplierForZeroBatchSize() throws Exception {
        List<String> elements = ListUtilities.newArrayList("a", "b");
        Consumer display = a -> System.out.println("Consumed: "+a);
        display.accept(elements);
        BatchProcessingCollector batch = new BatchProcessingCollector(0, display);
        Supplier<List<Throwable>> sup = batch.supplier();
        assertTrue(sup.get().isEmpty());
    }

    @Test
    public void canGetSupplierForNullConsumerAndZeroBatchSize() throws Exception {
        BatchProcessingCollector batch = new BatchProcessingCollector(0, null);
        Supplier<List<Throwable>> sup = batch.supplier();
        assertTrue(sup.get().isEmpty());
    }
}
