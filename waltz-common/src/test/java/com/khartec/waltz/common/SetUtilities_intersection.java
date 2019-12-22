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

import java.util.Collections;

import static com.khartec.waltz.common.SetUtilities.asSet;
import static com.khartec.waltz.common.SetUtilities.intersection;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SetUtilities_intersection {

    @Test
    public void intersectionOfEmptySetsGivesEmptySet() {
        assertTrue(intersection(Collections.emptySet(), Collections.emptySet()).isEmpty());
    }


    @Test
    public void intersectionOfDisjointSetsGivesEmptySet() {
        assertTrue(intersection(asSet("a", "b"), asSet("x", "y")).isEmpty());
    }


    @Test
    public void intersectionOfSetsGivesOnlyElementsThatAreInBoth() {
        assertEquals(
                "partial intersection",
                asSet("b"),
                intersection(asSet("a", "b"), asSet("b", "c")));

        assertEquals(
                "total intersection",
                asSet("a", "b"),
                intersection(asSet("a", "b"), asSet("b", "c", "a")));
    }


    @Test(expected = IllegalArgumentException.class)
    public void cannotIntersectNulls_both() {
        intersection(null, null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void cannotIntersectNulls_first() {
        intersection(null, Collections.emptySet());
    }


    @Test(expected = IllegalArgumentException.class)
    public void cannotIntersectNulls_second() {
        intersection(Collections.emptySet(), null);
    }

}
