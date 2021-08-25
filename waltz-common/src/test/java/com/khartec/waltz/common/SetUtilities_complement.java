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
import java.util.Set;

import static com.khartec.waltz.common.SetUtilities.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.runners.model.MultipleFailureException.assertEmpty;

public class SetUtilities_complement {

    @Test(expected = IllegalArgumentException.class)
    public void twoNullSets() {
        complement(null,null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void oneNullSet() {
        Set<String> abcSet = asSet("a", "b", "c");
        complement(abcSet,null);
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





   /* @Test
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


   */
}
