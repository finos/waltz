package com.khartec.waltz.common;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.function.Consumer;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.BatchProcessingCollector
 *
 * @author Diffblue JCover
 */

public class BatchProcessingCollectorTest {

    @Test
    public void accumulator() {
        @SuppressWarnings("unchecked")
        Consumer<List<String>> batchProcessor = null;
    }

    @Test
    public void characteristicsReturnsEmpty() {
        @SuppressWarnings("unchecked")
        Consumer<List<String>> batchProcessor = null;
        assertTrue(new BatchProcessingCollector<String>(1, batchProcessor).characteristics().isEmpty());
    }

    @Test
    public void combiner() {
        @SuppressWarnings("unchecked")
        Consumer<List<String>> batchProcessor = null;
    }

    @Test
    public void supplier() {
        @SuppressWarnings("unchecked")
        Consumer<List<String>> batchProcessor = null;
    }
}
