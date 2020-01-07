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

import java.util.Set;

import static com.khartec.waltz.common.SetUtilities.*;
import static java.util.Collections.emptySet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SetUtilities_minus {

    private Set<String> abcSet = asSet("a", "b", "c");
    private Set<String> abcdefSet = asSet("a", "b", "c", "d", "e", "f");
    private Set<String> bcdSet = asSet("b", "c", "d");
    private Set<String> defSet = asSet("d", "e", "f");


    @Test
    public void emptySetMinusEmptySetIsEmpty() {
        assertTrue("Empty set minus empty set should be an empty set",
                minus(emptySet(), emptySet()).isEmpty());
    }


    @Test
    public void setMinusItselfIsEmpty() {
        assertTrue("Set minus itself should be an empty set", minus(abcSet, abcSet).isEmpty());
    }


    @Test
    public void twoNonIntersectingSetsReturnFirstSet() {
        assertTrue(intersection(abcSet, defSet).isEmpty());
        assertEquals(abcSet, minus(abcSet, defSet));
    }


    @Test
    public void minusRemovesAllLaterSetElementsFromFirstSet(){
        assertEquals(asSet("a"), minus(abcSet, bcdSet));
        assertEquals(asSet("d"), minus(bcdSet, abcSet));
        assertEquals(asSet("a"), minus(abcdefSet, bcdSet, defSet));
        assertEquals(3, minus(abcdefSet, abcSet).size());
        assertEquals(emptySet(), minus(abcSet, bcdSet, abcdefSet));
    }

}
