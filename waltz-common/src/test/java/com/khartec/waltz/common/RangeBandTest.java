/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.common;

import org.junit.Test;

import static org.junit.Assert.*;

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


    @Test
    public void looksPretty() {
        assertEquals("0 - *", new RangeBand<>(0, null).toString());
        assertEquals("* - 10", new RangeBand<>(null, 10).toString());
        assertEquals("0 - 10", new RangeBand<>(0, 10).toString());
    }

}