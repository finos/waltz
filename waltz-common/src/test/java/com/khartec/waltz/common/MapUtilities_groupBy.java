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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;


public class MapUtilities_groupBy {

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
    public void canTransformValues() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b", "ccc");
        Map<Integer, Collection<String>> result = MapUtilities.groupBy(x -> x.length(), x -> x.toUpperCase(), xs);
        assertEquals(3, result.size());
        String b = CollectionUtilities.first(result.get(1));
        assertEquals("B", b);
    }
}
