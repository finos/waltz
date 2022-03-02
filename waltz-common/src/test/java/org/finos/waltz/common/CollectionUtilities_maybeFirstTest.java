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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;

import static org.finos.waltz.common.CollectionUtilities.map;
import static org.finos.waltz.common.CollectionUtilities.maybeFirst;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CollectionUtilities_maybeFirstTest {

    @Test
    public void callingOnNullCollectionGivesEmpty() {
        assertEquals(CollectionUtilities.maybeFirst(null), Optional.empty());
    }


    @Test
    public void callingOnEmptyCollectionGivesEmpty() {
        assertEquals(CollectionUtilities.maybeFirst(ListUtilities.newArrayList()), Optional.empty());
    }


    @Test
    public void callingOnCollectionWithNullPredicateThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> CollectionUtilities.maybeFirst(ListUtilities.newArrayList(), null));
    }


    @Test
    public void callingOnNullCollectionWithPredicateGivesEmpty() {
        assertEquals(CollectionUtilities.maybeFirst((Collection<Integer>) null, x -> x > 10), Optional.empty());
    }


    @Test
    public void givesEmptyIfNoElementsPassPredicate () {
        assertEquals(CollectionUtilities.maybeFirst(ListUtilities.newArrayList(1, 2, 3), x -> x > 10), Optional.empty());
    }


    @Test
    public void givesFirstPassingElement () {
        assertEquals(CollectionUtilities.maybeFirst(ListUtilities.newArrayList(1, 20, 3), x -> x > 10), Optional.of(20));
    }


}
