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
import java.util.Set;

import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.SetUtilities.complement;
import static org.finos.waltz.common.SetUtilities.union;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SetUtilities_complementTest {

    @Test
    public void twoNullSets() {
        assertThrows(IllegalArgumentException.class,
                () -> complement(null, null));
    }

    @Test
    public void oneNullSet() {
        Set<String> abcSet = asSet("a", "b", "c");
        assertThrows(IllegalArgumentException.class,
                () -> complement(abcSet, null));
    }

    @Test
    public void complimentOfEmptySetsIsTheEmptySet() {

        Set<Object> result = complement(Collections.emptySet(), Collections.emptySet());
        assertTrue(result.isEmpty());
    }

    @Test
    public void  complimentOfOneSetAndEmptySetIsTheSet() {
        Set<String> abcSet = asSet("a", "b", "c");
        Set<String> emptySet = asSet();
        Set<String> result = complement(abcSet , emptySet );

        assertEquals(abcSet, result);
        Set<String> result2 = complement(emptySet,abcSet );

        assertEquals(abcSet, result2);

    }
    @Test
    public  void twoExclusiveSetsIsTheUnionOfSets(){
        Set<String> abcSet = asSet("a", "b", "c");
        Set<String> defSet = asSet("d", "e", "f");
        Set<String> result = complement(abcSet , defSet );

        assertEquals(union(abcSet,defSet), result);


    }
    @Test
    public void twoSimilarSetsProduceOneUniqueSet(){
        Set<String> abcSet = asSet("a", "b", "c");
        Set<String> bcdSet = asSet("b", "c", "d");
        Set<String> result = complement(abcSet,bcdSet);

        assertEquals(asSet("a","d"),result);
    }

    @Test
    public void complimentOfTwoIdenticalSetsIsEmpty() {
        Set<String> abcSet = asSet("a", "b", "c");
        Set<String> result = complement(abcSet ,abcSet );
        assertTrue(result.isEmpty());
    }

}
