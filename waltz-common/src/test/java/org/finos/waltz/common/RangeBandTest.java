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

package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class RangeBandTest {

    public static final RangeBand<Integer> range10_20 = new RangeBand<>(10, 20);
    public static final RangeBand<Integer> anotherRange10_20 = new RangeBand<>(10, 20);
    public static final RangeBand<Integer> range20_30 = new RangeBand<>(20, 30);
    public static final RangeBand<Integer> rangeNull_20 = new RangeBand<>(null, 20);
    public static final RangeBand<Integer> range10_Null = new RangeBand<>(10, null);

    @Test
    public void lowAndHighCannotBeNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new RangeBand<>(null, null));
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
    public void testReturnsTrueIfWithinRange() {
        assertTrue(range10_20.test(15));
    }

    @Test
    public void testReturnsTrueIfOnUpperBound() {
        assertTrue(range10_20.test(20));
    }

    @Test
    public void testReturnsTrueIfOnLowerBound() {
        assertTrue(range10_20.test(10));
    }

    @Test
    public void testReturnsFalseIfBelowLowBound() {
        assertFalse(range10_20.test(9));
    }

    @Test
    public void testReturnsFalseIfAboveHighBound() {
        assertFalse(range10_20.test(21));
    }

    @Test
    public void simpleGetLow(){
        assertEquals(Optional.of(10), Optional.of(range10_20.getLow()));
    }

    @Test
    public void simpleGetHigh(){
        assertEquals(Optional.of(20),Optional.of(range10_20.getHigh()));
    }

    @Test
    public void simpleEqualsTrue(){
        assertTrue(range10_20.equals(anotherRange10_20));
    }

    @Test
    public void simpleEqualsFalse(){
        assertFalse(range10_20.equals(range20_30));
    }

    @Test
    public void simpleEqualsWithLowNull(){
        assertFalse(range10_20.equals(rangeNull_20));
    }

    @Test
    public void simpleEqualsWithHighNull(){
        assertFalse(range10_20.equals(range10_Null));
    }

    @Test
    public void simpleHashCode(){
        assertEquals(330, range10_20.hashCode());
    }

    @Test
    public void simpleHashCodeWithLowNull(){
        assertEquals(20, rangeNull_20.hashCode());
    }

    @Test
    public void simpleHashCodeWithHighNull(){
        assertEquals(310, range10_Null.hashCode());
    }

    @Test
    public void ifNoLowerBoundSpecifiedAssumeNoLowerLimit() {
        RangeBand<Integer> noLowBound = new RangeBand<>(null, 10);

        assertTrue(noLowBound.contains(-10));
        assertTrue(noLowBound.contains(10));
        assertFalse(noLowBound.contains(11));
        assertTrue(noLowBound.test(-10));
        assertTrue(noLowBound.test(10));
        assertFalse(noLowBound.test(11));
    }

    @Test
    public void ifNoHighBoundSpecifiedAssumeNoUpperLimit() {
        RangeBand<Integer> noHighBound = new RangeBand<>(0, null);

        assertTrue(noHighBound.contains(100000));
        assertTrue(noHighBound.contains(0));
        assertFalse(noHighBound.contains(-1));
        assertTrue(noHighBound.test(100000));
        assertTrue(noHighBound.test(0));
        assertFalse(noHighBound.test(-1));
    }

    @Test
    public void lowCannotBeHigherThanHigh() {
        assertThrows(IllegalArgumentException.class,
                () -> new RangeBand<>(10, 5));
    }


    @Test
    public void looksPretty() {
        assertEquals("0 - *", new RangeBand<>(0, null).toString());
        assertEquals("* - 10", new RangeBand<>(null, 10).toString());
        assertEquals("0 - 10", new RangeBand<>(0, 10).toString());
    }

}