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

import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Optional;

import static org.finos.waltz.common.CollectionUtilities.maybeFirst;
import static org.junit.Assert.assertEquals;

public class CollectionUtilities_maybeFirst {

    @Test
    public void callingOnNullCollectionGivesEmpty() {
        Assert.assertEquals(CollectionUtilities.maybeFirst(null), Optional.empty());
    }


    @Test
    public void callingOnEmptyCollectionGivesEmpty() {
        Assert.assertEquals(CollectionUtilities.maybeFirst(ListUtilities.newArrayList()), Optional.empty());
    }


    @Test(expected = IllegalArgumentException.class)
    public void callingOnCollectionWithNullPredicateThrows() {
        Assert.assertEquals(CollectionUtilities.maybeFirst(ListUtilities.newArrayList(), null), Optional.empty());
    }


    @Test
    public void callingOnNullCollectionWithPredicateGivesEmpty() {
        Assert.assertEquals(CollectionUtilities.maybeFirst((Collection<Integer>) null, x -> x > 10), Optional.empty());
    }


    @Test
    public void givesEmptyIfNoElementsPassPredicate () {
        Assert.assertEquals(CollectionUtilities.maybeFirst(ListUtilities.newArrayList(1,2,3), x -> x > 10), Optional.empty());
    }


    @Test
    public void givesFirstPassingElement () {
        Assert.assertEquals(CollectionUtilities.maybeFirst(ListUtilities.newArrayList(1,20,3), x -> x > 10), Optional.of(20));
    }


}
