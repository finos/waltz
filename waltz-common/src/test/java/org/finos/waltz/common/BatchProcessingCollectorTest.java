package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BatchProcessingCollectorTest {

    @Test
    public void batchingWorksAndReportsNumberOfItemsProcessed() {
        ArrayList<String> data = ListUtilities.newArrayList("A", "B", "C", "D");
        List<String> batched = new ArrayList<>();

        BatchProcessingCollector<String> batchProcessor = new BatchProcessingCollector<>(
                3,
                xs -> {
                    batched.add(StringUtilities.join(xs, "!"));
                });

        Integer rc = data
                .stream()
                .collect(batchProcessor);

        assertEquals(4, rc, "Should have processed all input items");
        assertEquals(ListUtilities.asList("A!B!C", "D"), batched);
    }
}
