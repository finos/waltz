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

import static com.khartec.waltz.common.CollectionUtilities.filter;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class CollectionUtilities_filter {

    private static final List<String> words = newArrayList(
            "hello",
            "world");


    @Test
    public void filteringOutAllElementsGivesEmptyCollection() {
        assertTrue(filter(words, w -> w.startsWith("z"))
                .isEmpty());
    }


    @Test
    public void filteringWorks() {
        Collection<String> wordsStartingWithH = filter(words, w -> w.startsWith("h"));
        assertEquals(1, wordsStartingWithH.size());
        assertTrue(wordsStartingWithH.contains("hello"));
    }


    @Test(expected = IllegalArgumentException.class)
    public void filteringANullCollectionThrowsIllegalArgException() {
        filter(null, x -> true);
    }


    @Test(expected = IllegalArgumentException.class)
    public void filteringWithoutAFilterPredicatedThrowsIllegalArgumentException() {
        filter(words, null);
    }

}
