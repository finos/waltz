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

import java.util.List;
import java.util.Optional;

import static org.finos.waltz.common.CollectionUtilities.head;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CollectionUtilities_headTest {

    private static final List<String> words = newArrayList(
            "hello",
            "world");


    @Test
    public void ifCollectionIsNullThenEmptyIsReturned() {
        assertEquals(
                Optional.empty(),
                head(null));
    }


    @Test
    public void ifListIsNonEmptyReturnsFirstItem() {
        assertEquals(
                Optional.of("hello"),
                head(words));
    }


    @Test
    public void emptyReturnedIfNoElements() {
        assertEquals(
                Optional.empty(),
                head(newArrayList()));
    }

}
