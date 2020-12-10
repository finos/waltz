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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ArrayUtilities_initial {

    @Test(expected = IllegalArgumentException.class)
    public void askingForTheInitialPartOfAnEmptyArrayThrowsException() {
        String[] xs = {};
        ArrayUtilities.initial(xs);
    }


    @Test(expected = IllegalArgumentException.class)
    public void askingForTheInitialPartOfANullArrayThrowsException() {
        ArrayUtilities.initial(null);
    }


    @Test
    public void askingForTheInitialPartOfOneElementArrayGivesAnEmptyArray() {
        String[] xs = {"A"};
        assertArrayEquals(new String[0], ArrayUtilities.initial(xs));
    }


    @Test
    public void askingForTheInitialPartOfMultiElementArrayGivesAllButLast() {
        String[] xs = {"A", "B", "C"};
        assertArrayEquals(new String[] {"A", "B"}, ArrayUtilities.initial(xs));
    }

}
