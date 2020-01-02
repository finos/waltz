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

import java.util.Map;

import static org.junit.Assert.assertEquals;


public class MapBuilderTest {

    @Test
    public void simple() {

        Map<String, Integer> m =  new MapBuilder<String, Integer>()
                .add("a", 1)
                .build();

        assertEquals(1, m.size());
        assertEquals(Integer.valueOf(1), m.get("a"));

    }


    @Test
    public void differingTypes() {

        Map<String, Object> m =  new MapBuilder<String, Object>()
                .add("a", 1)
                .add("b", "bob")
                .build();

        assertEquals(2, m.size());
        assertEquals(Integer.valueOf(1), m.get("a"));
        assertEquals("bob", m.get("b"));
    }

}
