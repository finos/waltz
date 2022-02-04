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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class MapUtilities_groupByTest {

    @Test
    public void simpleGroupBy() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b" , "ccc");
        Map<Integer, Collection<String>> result = MapUtilities.groupBy(x -> x.length(), xs);
        assertEquals(3, result.size());

        // 2 of length 2
        assertEquals(2, result.get(2).size());
        // 1 of length 1
        assertEquals(1, result.get(1).size());
        // 1 of length 3
        assertEquals(1, result.get(3).size());
    }

    @Test
    public void simpleGroupNullList() {
        List<String> xs = null;

        assertThrows(IllegalArgumentException.class,
                () -> MapUtilities.groupBy(x -> x.length(), xs));

    }

    @Test
    public void simpleGroupEmptyList() {
        List<String> xs = new ArrayList();
        Map<Integer, Collection<String>> result = MapUtilities.groupBy(x -> x.length(), xs);
        assertEquals(0, result.size());
        assertTrue(result.isEmpty());
    }

    @Test
    public void simpleGroupNullFunc() {
        List<String> xs = new ArrayList();
        assertThrows(IllegalArgumentException.class,
                () -> MapUtilities.groupBy(null, xs));
    }

    @Test
    public void canTransformValues() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b", "ccc");
        Map<Integer, Collection<String>> result = MapUtilities.groupBy(x -> x.length(), x -> x.toUpperCase(), xs);
        assertEquals(3, result.size());
        String b = CollectionUtilities.first(result.get(1));
        assertEquals("B", b);
    }

    @Test
    public void canTransformEmptyList() {
        List<String> xs = new ArrayList();
        Map<Integer, Collection<String>> result = MapUtilities.groupBy(x -> x.length(), x -> x.toUpperCase(), xs);
        assertEquals(0, result.size());
        assertTrue(result.isEmpty());
    }

    @Test
    public void cannotTransformNullList() {
        List<String> xs = null;

        assertThrows(IllegalArgumentException.class,
                () -> MapUtilities.groupBy(x -> x.length(), x -> x.toUpperCase(), xs));
    }

    @Test
    public void cannotTransformWithNullFirstFunc() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b", "ccc");
        assertThrows(IllegalArgumentException.class,
                () -> MapUtilities.groupBy(null, x -> x.toUpperCase(), xs));
    }

    @Test
    public void cannotTransformWithNullSecondFunc() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b", "ccc");
        assertThrows(IllegalArgumentException.class,
                () -> MapUtilities.groupBy(x -> x.length(), null, xs));
    }

    @Test
    public void cannotTransformWithAllNullParams() {
        List<String> xs = null;
        assertThrows(IllegalArgumentException.class,
                () -> MapUtilities.groupBy(null, null, xs));
    }

    //toggled params

    @Test
    public void toggledSimpleGroupBy() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b" , "ccc");
        Map<Integer, Collection<String>> result = MapUtilities.groupBy(xs, x -> x.length());
        assertEquals(3, result.size());
        // 2 of length 2
        assertEquals(2, result.get(2).size());
        // 1 of length 1
        assertEquals(1, result.get(1).size());
        // 1 of length 3
        assertEquals(1, result.get(3).size());
    }

    @Test
    public void toggledSimpleGroupNullList() {
        List<String> xs = null;
        assertThrows(IllegalArgumentException.class,
                () -> MapUtilities.groupBy(xs, x -> x.length()));
    }

    @Test
    public void toggledSimpleGroupEmptyList() {
        List<String> xs = new ArrayList();
        Map<Integer, Collection<String>> result = MapUtilities.groupBy(xs, x -> x.length());
        assertEquals(0, result.size());
        assertTrue(result.isEmpty());
    }

    @Test
    public void toggledSimpleGroupNullFunc() {
        List<String> xs = new ArrayList();
        assertThrows(IllegalArgumentException.class,
                () -> MapUtilities.groupBy(xs, null));
    }

    @Test
    public void toggledCanTransformValues() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b", "ccc");
        Map<Integer, Collection<String>> result = MapUtilities.groupBy(xs, x -> x.length(), x -> x.toUpperCase());
        assertEquals(3, result.size());
        String b = CollectionUtilities.first(result.get(1));
        assertEquals("B", b);
    }

    @Test
    public void toggledCanTransformEmptyList() {
        List<String> xs = new ArrayList();
        Map<Integer, Collection<String>> result = MapUtilities.groupBy(xs, x -> x.length(), x -> x.toUpperCase());
        assertEquals(0, result.size());
        assertTrue(result.isEmpty());
    }

    @Test
    public void toggledCannotTransformNullList() {
        List<String> xs = null;

        assertThrows(IllegalArgumentException.class,
                () -> MapUtilities.groupBy(xs, x -> x.length(), x -> x.toUpperCase()));
    }

    @Test
    public void toggledCannotTransformWithNullFirstFunc() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b", "ccc");

        assertThrows(IllegalArgumentException.class,
                () -> MapUtilities.groupBy(xs, null, x -> x.toUpperCase()));
    }

    @Test
    public void toggledCannotTransformWithNullSecondFunc() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b", "ccc");
        assertThrows(IllegalArgumentException.class,
                () -> MapUtilities.groupBy(xs, x -> x.length(), null));
    }

    @Test
    public void toggledCannotTransformWithAllNullParams() {
        List<String> xs = null;

        assertThrows(IllegalArgumentException.class,
                () -> MapUtilities.groupBy(xs, null, null));
    }
}
