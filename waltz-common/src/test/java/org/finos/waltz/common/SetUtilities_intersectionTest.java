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

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class SetUtilities_intersectionTest {

    @Test
    public void intersectionOfEmptySetsGivesEmptySet() {
        assertTrue(SetUtilities.intersection(Collections.emptySet(), Collections.emptySet()).isEmpty());
    }


    @Test
    public void intersectionOfDisjointSetsGivesEmptySet() {
        assertTrue(SetUtilities.intersection(SetUtilities.asSet("a", "b"), SetUtilities.asSet("x", "y")).isEmpty());
    }


    @Test
    public void intersectionOfSetsGivesOnlyElementsThatAreInBoth() {
        assertEquals(
                //"partial intersection",
                SetUtilities.asSet("b"),
                SetUtilities.intersection(SetUtilities.asSet("a", "b"), SetUtilities.asSet("b", "c")));

        assertEquals(
                //"total intersection",
                SetUtilities.asSet("a", "b"),
                SetUtilities.intersection(SetUtilities.asSet("a", "b"), SetUtilities.asSet("b", "c", "a")));
    }


    @Test
    public void cannotIntersectNulls_both() {
        assertThrows(IllegalArgumentException.class,
                () -> SetUtilities.intersection(null, null));
    }


    @Test
    public void cannotIntersectNulls_first() {
        assertThrows(IllegalArgumentException.class,
                () -> SetUtilities.intersection(null, Collections.emptySet()));
    }


    @Test
    public void cannotIntersectNulls_second() {
        assertThrows(IllegalArgumentException.class,
                () -> SetUtilities.intersection(Collections.emptySet(), null));
    }

}
