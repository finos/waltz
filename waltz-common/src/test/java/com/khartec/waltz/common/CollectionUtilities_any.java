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
import java.util.List;
import java.util.function.Predicate;

import static com.khartec.waltz.common.CollectionUtilities.any;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class CollectionUtilities_any {

    private static final List<String> words = newArrayList(
            "hello",
            "world");


    @Test
    public void anyOnAnEmptyCollectionGivesFalse() {
        Predicate<String> stringPredicate = w -> true;
        assertFalse(any(Collections.emptyList(), stringPredicate));
    }


    @Test
    public void anyWorks() {
        assertTrue(any(words, w -> w.startsWith("h")));
        assertFalse(any(words, w -> w.startsWith("z")));
    }


    @Test(expected = IllegalArgumentException.class)
    public void anyOnANullCollectionThrowsIllegalArgException() {
        any(null, x -> true);
    }


    @Test(expected = IllegalArgumentException.class)
    public void anyWithoutAPredicatedThrowsIllegalArgumentException() {
        any(words, null);
    }

}
