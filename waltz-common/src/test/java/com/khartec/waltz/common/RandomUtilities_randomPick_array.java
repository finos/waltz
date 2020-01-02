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

import static com.khartec.waltz.common.RandomUtilities.randomPick;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class RandomUtilities_randomPick_array {

    @Test(expected = IllegalArgumentException.class)
    public void cannotRandomPickFromNothing() {
        randomPick();
    }


    @Test
    public void randomPickFromPoolOfOneReturnsThatOne() {
        assertEquals("a", randomPick("a"));
    }


    @Test
    public void randomPickFromPoolIsMemberOfPool() {
        assertTrue(SetUtilities.fromArray("a", "b")
                .contains(randomPick("a", "b")));
    }

}
