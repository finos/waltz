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

import static junit.framework.TestCase.assertEquals;

public class ArrayUtilities_sum {

    @Test(expected = IllegalArgumentException.class)
    public void cannotSumNull() {
        ArrayUtilities.sum(null);
    }


    @Test
    public void sumOfEmptyArrayIsZero() {
        assertEquals(0, ArrayUtilities.sum(new int[]{}));
    }


    @Test
    public void canSumSingleElementArray() {
        assertEquals(2, ArrayUtilities.sum(new int[]{2}));
    }


    @Test
    public void canSumMultiElementArray() {
        assertEquals(14, ArrayUtilities.sum(new int[]{2,4,6,2}));
    }

}
