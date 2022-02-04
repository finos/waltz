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

import java.util.*;

import static org.finos.waltz.common.RandomUtilities.randomPick;
import static org.junit.jupiter.api.Assertions.*;

public class RandomUtilities_randomPickTest {

    @Test
    public void simpleRandomPickArray() {
        assertThrows(IllegalArgumentException.class,
                RandomUtilities::randomPick);
    }


    @Test
    public void randomPickArrayFromPoolOfOne() {
        assertEquals("a", randomPick("a"));
    }


    @Test
    public void randomPickArrayFromPool() {
        assertTrue(SetUtilities.fromArray("a", "b")
                .contains(randomPick("a", "b")));
    }

    @Test
    public void randomPickArrayFromNull() {
        String[] str = null;
        assertThrows(IllegalArgumentException.class,
                () -> randomPick(str));
    }

    @Test
    public void simpleRandomPickCollection() {
        Collection<String> ele = new ArrayList<>();
        assertThrows(IllegalArgumentException.class,
                () -> randomPick(ele));
    }


    @Test
    public void randomPickCollFromPoolOfOne() {
        Collection<String> ele = ListUtilities.newArrayList("a");
        assertEquals("a", randomPick(ele));
    }

    @Test
    public void randomPickCollFromString() {
        assertEquals("a", randomPick(Collections.singleton("a")));
    }


    @Test
    public void randomPickCollFromPool() {
        Collection<String> ele = ListUtilities.newArrayList("a","b");
        assertTrue(ListUtilities.asList("a", "b")
                .contains(randomPick(ele)));
    }

    @Test
    public void randomPickCollFromNull() {
        Collection<String> str = null;
        assertThrows(IllegalArgumentException.class,
                () -> randomPick(str));
    }


    @Test
    public void randomPickWithHowManyEmptyColl() {
        Collection<String> ele = new ArrayList<>();
        List result = randomPick(ele, 1);
        assertEquals(0,result.size());
    }


    @Test
    public void randomPickWithZeroHowMany() {
        Collection<String> ele = ListUtilities.newArrayList("a");
        List result = randomPick(ele,0);
        assertEquals(0,result.size());
    }

    @Test
    public void simpleRandomPickWithHowMany() {
        Collection ele = ListUtilities.newArrayList("a");
        assertEquals(ele, randomPick(ele,1));
    }


    @Test
    public void randomPickWithHowManyFromPool1() {
        Collection<String> ele = ListUtilities.newArrayList("a","b");
        List compareList = new ArrayList();
        compareList.add(ListUtilities.newArrayList("a"));
        compareList.add(ListUtilities.newArrayList("b"));
        assertTrue(compareList.contains(randomPick(ele,1)));
    }

    @Test
    public void randomPickWithHowManyFromPool2() {
        Collection<String> ele = ListUtilities.newArrayList("a","b");
        assertTrue(ListUtilities.asList("a", "b")
                .containsAll(randomPick(ele,3)));
    }

    @Test
    public void randomPickWithHowManyFromNull() {
        Collection<String> str = null;
        List result = randomPick(str,1);
        assertEquals(0,result.size());
    }

    @Test
    public void randomPickWithZeroHowManyFromNull() {
        Collection<String> str = null;
        List result = randomPick(str,0);
        assertEquals(0,result.size());
    }

    @Test
    public void randomPickWithEmptyList() {
        List ele = new ArrayList<>();
        assertThrows(IllegalArgumentException.class,
                () -> randomPick(ele));
    }


    @Test
    public void randomPickListWithSizeOne() {
        List<String> ele = ListUtilities.newArrayList("a");
        String result = randomPick(ele);
        assertTrue(ele.contains(result));
    }


    @Test
    public void randomPickListWithSizeTwo() {
        List<String> ele = ListUtilities.newArrayList("a","b");
        String result = randomPick(ele);
        assertTrue(ele.contains(result));
    }

    @Test
    public void randomPickWithListNull() {
        List<String> str = null;
        assertThrows(NullPointerException.class,
                () -> randomPick(str));
    }
}
