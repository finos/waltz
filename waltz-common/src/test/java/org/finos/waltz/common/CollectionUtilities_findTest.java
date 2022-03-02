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

import static org.finos.waltz.common.CollectionUtilities.filter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CollectionUtilities_findTest {

    private static final List<String> words = ListUtilities.newArrayList(
            "a",
            "hello",
            "world");


    @Test
    public void predicateCannotBeNull() {
        assertThrows(IllegalArgumentException.class,
                () -> CollectionUtilities.find(null, words));
    }


    @Test
    public void collectionCannotBeNull() {
        assertThrows(IllegalArgumentException.class,
                () -> CollectionUtilities.find(d -> true, null));
    }


    @Test
    public void canFindThings() {
        Optional<String> r = CollectionUtilities.find(
                d -> d.contains("e"),
                words);
        assertEquals(Optional.of("hello"), r);
    }


    @Test
    public void emptyReturnedIfNotFound() {
        Optional<String> r = CollectionUtilities.find(
                d -> d.contains("z"),
                words);
        assertEquals(Optional.empty(), r);
    }

}
