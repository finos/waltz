package com.khartec.waltz.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.StreamUtilities
 *
 * @author Diffblue JCover
 */

public class StreamUtilitiesTest {

    @Test
    public void applyReturnsFoo() {
        assertThat(StreamUtilities.<String>tap().apply("foo"), is("foo"));
    }

    @Test
    public void batchProcessingCollectorBatchSizeIsOne() {
        @SuppressWarnings("unchecked")
        Consumer<List<String>> batchProcessor = null;
    }

    @Test
    public void mkSiphon() {
        assertTrue(StreamUtilities.<String>mkSiphon(new RangeBand<String>("foo", "foo")).getResults().isEmpty());
    }

    @Test
    public void ofNullableArrayArrIsBroadway() {
        String[] arr = new String[] { "Broadway" };
        String[] array = StreamUtilities.<String>ofNullableArray(arr).toArray(String[]::new);
        assertArrayEquals(new String[] { "Broadway" }, array);
    }

    @Test
    public void ofNullableArrayArrIsNull() {
        String[] array = StreamUtilities.<String>ofNullableArray(null).toArray(String[]::new);
        assertArrayEquals(new String[] { }, array);
    }
}
