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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.common.SetUtilities.unionAll;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SetUtilities_unionAll {

    @Test
    public void unionAllOfNothingGivesEmptySet() {
        assertTrue(unionAll(Collections.emptyList()).isEmpty());
    }


    @Test(expected = IllegalArgumentException.class)
    public void cannotUnionAllNull() {
        unionAll(null);
    }


    @Test
    public void unionAllTakesACollectionOfCollectionsAndReturnsTheUnionOfAllSubCollections() {
        ArrayList<String> listA = newArrayList("a", "b");
        ArrayList<String> listB = newArrayList("b", "c");
        Collection<String> r = unionAll(newArrayList(listA, listB));
        assertTrue(! r.isEmpty());
        assertEquals(3, r.size());
        assertTrue(r.contains("a"));
        assertTrue(r.contains("b"));
        assertTrue(r.contains("c"));
    }

}
