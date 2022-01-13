package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class BatchProcessingCollector_characteristicsTest {
    @Test
    public void canGetCharacteristics() {
        List<String> elements = ListUtilities.newArrayList("a", "b");
        Consumer display = a -> System.out.println("Consumed: "+a);
        display.accept(elements);
        BatchProcessingCollector batch = new BatchProcessingCollector(10, display);
        Set output = batch.characteristics();
        assertTrue(output.isEmpty());
    }

    @Test
    public void canGetCharacteristicsForNullConsumer() {
        BatchProcessingCollector batch = new BatchProcessingCollector(10, null);
        Set output = batch.characteristics();
        assertTrue(output.isEmpty());
    }

    @Test
    public void canGetCharacteristicsForZeroBatchsize() {
        BatchProcessingCollector batch = new BatchProcessingCollector(0, null);
        Set output = batch.characteristics();
        assertTrue(output.isEmpty());
    }
}
