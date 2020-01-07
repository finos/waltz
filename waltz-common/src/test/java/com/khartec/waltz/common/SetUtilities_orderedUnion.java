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

import static com.khartec.waltz.common.SetUtilities.asSet;
import static com.khartec.waltz.common.SetUtilities.orderedUnion;
import static java.util.Collections.emptySet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SetUtilities_orderedUnion {

    private Set<String> abcSet = asSet("a", "b", "c");
    private Set<String> bcaSet = asSet("b", "c", "a");
    private Set<String> bcdSet = asSet("b", "c", "d");
    private Set<String> abcdefSet = asSet("a", "b", "c", "d", "e", "f");
    private Set<String> defSet = asSet("d", "e", "f");
    private Set<String> efaSet = asSet( "e", "f", "a");


    @Test
    public void unionTwoEmptySetReturnEmptySet(){
        assertTrue(orderedUnion(emptySet(), emptySet()).isEmpty());
    }


    @Test
    public void unionEmptySetWithNotEmptySetReturnsSet(){
        assertEquals(abcSet, orderedUnion(abcSet, emptySet()));
        assertEquals(abcSet, orderedUnion(bcaSet, emptySet()));
    }


    @Test
    public void unionTwoSetsReturnsOrderedSetOfAllElements(){
        assertEquals(abcdefSet, orderedUnion(abcSet, defSet));
        assertEquals(abcdefSet, orderedUnion(defSet, abcSet));
        assertEquals(6, orderedUnion(abcSet, defSet).size());
        assertEquals(asSet("a", "b", "c", "d"), orderedUnion(bcdSet, abcSet));
        assertEquals(asSet("a", "d", "e", "f"), orderedUnion(efaSet, defSet));
    }
}
