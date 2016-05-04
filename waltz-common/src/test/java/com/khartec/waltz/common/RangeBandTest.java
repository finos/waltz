package com.khartec.waltz.common;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RangeBandTest {

    public static final RangeBand<Integer> range10_20 = new RangeBand<>(10, 20);

    @Test(expected = IllegalArgumentException.class)
    public void lowAndHighCannotBeNull() {
        new RangeBand<>(null, null);
    }

    @Test
    public void containsReturnsTrueIfWithinRange() {
        assertTrue(range10_20.contains(15));
    }

    @Test
    public void containsReturnsTrueIfOnUpperBound() {
        assertTrue(range10_20.contains(20));
    }

    @Test
    public void containsReturnsTrueIfOnLowerBound() {
        assertTrue(range10_20.contains(10));
    }

    @Test
    public void containsReturnsFalseIfBelowLowBound() {
        assertFalse(range10_20.contains(9));
    }

    @Test
    public void containsReturnsFalseIfAboveHighBound() {
        assertFalse(range10_20.contains(21));
    }

    @Test
    public void ifNoLowerBoundSpecifiedAssumeNoLowerLimit() {
        RangeBand<Integer> noLowBound = new RangeBand<>(null, 10);

        assertTrue(noLowBound.contains(-10));
        assertTrue(noLowBound.contains(10));
        assertFalse(noLowBound.contains(11));
    }

    @Test
    public void ifNoHighBoundSpecifiedAssumeNoUpperLimit() {
        RangeBand<Integer> noHighBound = new RangeBand<>(0, null);

        assertTrue(noHighBound.contains(100000));
        assertTrue(noHighBound.contains(0));
        assertFalse(noHighBound.contains(-1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void lowCannotBeHigherThanHigh() {
        new RangeBand<>(10, 5);
    }


}