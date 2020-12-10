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

import java.util.List;

import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ListUtilities_compact {

    @Test
    public void compactFiltersNullsFromCollection() {
        List<String> compacted = ListUtilities.compact(newArrayList("a", null, "b"));
        assertEquals(2, compacted.size());
        assertTrue(compacted.contains("a"));
        assertTrue(compacted.contains("b"));
    }


    @Test
    public void collectionOfAllNullsWillReturnEmptyList() {
        List<String> compacted = ListUtilities.compact(newArrayList(null, null));
        assertTrue(compacted.isEmpty());
    }


    @Test(expected = IllegalArgumentException.class)
    public void aNullCollectionWillThrowIllegalArgException() {
        ListUtilities.compact(null);
    }

}
