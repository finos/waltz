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

import java.util.Collection;
import java.util.Optional;

import static com.khartec.waltz.common.CollectionUtilities.maybeFirst;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static org.junit.Assert.assertEquals;

public class CollectionUtilities_maybeFirst {

    @Test
    public void callingOnNullCollectionGivesEmpty() {
        assertEquals(maybeFirst(null), Optional.empty());
    }


    @Test
    public void callingOnEmptyCollectionGivesEmpty() {
        assertEquals(maybeFirst(newArrayList()), Optional.empty());
    }


    @Test(expected = IllegalArgumentException.class)
    public void callingOnCollectionWithNullPredicateThrows() {
        assertEquals(maybeFirst(newArrayList(), null), Optional.empty());
    }


    @Test
    public void callingOnNullCollectionWithPredicateGivesEmpty() {
        assertEquals(maybeFirst((Collection<Integer>) null, x -> x > 10), Optional.empty());
    }


    @Test
    public void givesEmptyIfNoElementsPassPredicate () {
        assertEquals(maybeFirst(newArrayList(1,2,3), x -> x > 10), Optional.empty());
    }


    @Test
    public void givesFirstPassingElement () {
        assertEquals(maybeFirst(newArrayList(1,20,3), x -> x > 10), Optional.of(20));
    }


}
