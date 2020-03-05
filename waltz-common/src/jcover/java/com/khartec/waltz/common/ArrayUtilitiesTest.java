package com.khartec.waltz.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.common.ArrayUtilities
 *
 * @author Diffblue JCover
 */

public class ArrayUtilitiesTest {

    @Test
    public void allTsIsBarReturnsFalse() {
        String[] ts = new String[] { "bar" };
        assertThat(ArrayUtilities.<String>all(ts, new RangeBand<String>("foo", "foo")), is(false));
    }

    @Test
    public void allTsIsEmpty() {
        assertThat(ArrayUtilities.<String>all(new String[] { }, new RangeBand<String>("foo", "foo")), is(true));
    }

    @Test
    public void allTsIsFoo() {
        String[] ts = new String[] { "foo" };
        assertThat(ArrayUtilities.<String>all(ts, new RangeBand<String>("bar", "foo")), is(true));
    }

    @Test
    public void initialBitsIsFooReturnsEmpty() {
        String[] bits = new String[] { "foo" };
        assertArrayEquals(new String[] { }, ArrayUtilities.<String>initial(bits));
    }

    @Test
    public void isEmpty() {
        assertThat(ArrayUtilities.<String>isEmpty(new String[] { }), is(true));
        assertThat(ArrayUtilities.<String>isEmpty(null), is(true));
    }

    @Test
    public void isEmptyArrIsFooReturnsFalse() {
        String[] arr = new String[] { "foo" };
        assertThat(ArrayUtilities.<String>isEmpty(arr), is(false));
    }

    @Test
    public void lastArrIsSmithReturnsSmith() {
        String[] arr = new String[] { "Smith" };
        assertThat(ArrayUtilities.<String>last(arr), is("Smith"));
    }

    @Test
    public void sumArrIsEmptyReturnsZero() {
        assertThat(ArrayUtilities.sum(new int[] { }), is(0));
    }

    @Test
    public void sumArrIsOneReturnsOne() {
        int[] arr = new int[] { 1 };
        assertThat(ArrayUtilities.sum(arr), is(1));
    }
}
