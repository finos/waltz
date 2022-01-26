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

import java.util.ArrayList;

import static org.finos.waltz.common.TestUtilities.assertLength;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListUtilities_newArrayListTest {

    @Test
    public void singleElement() {
        ArrayList<String> arr = ListUtilities.newArrayList("a");
        TestUtilities.assertLength(arr, 1);
        assertEquals("a", arr.get(0));
    }

    @Test
    public void multipleElements() {
        ArrayList<String> arr = ListUtilities.newArrayList("a", "b");
        TestUtilities.assertLength(arr, 2);
        assertEquals("a", arr.get(0));
        assertEquals("b", arr.get(1));
    }

    @Test
    public void zeroElements() {
        ArrayList<String> arr = ListUtilities.newArrayList();
        TestUtilities.assertLength(arr, 0);
    }
}
