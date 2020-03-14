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
import java.util.List;
import java.util.Objects;

import static com.khartec.waltz.common.CollectionUtilities.all;
import static com.khartec.waltz.common.ListUtilities.newArrayList;

public class CollectionUtilities_all {

    private static final List<String> words = newArrayList(
            "hello",
            "",
            "world");

    private static final List<String> empty = newArrayList();


    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionIfCollectionIsNull() {
        all((Collection<String>) null, StringUtilities::isEmpty);
    }


    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionIfPredicateIsNull() {
        all(words, null);
    }


    @Test
    public void givesTrueIfAllElementsPassPredicate() {
        all(words, Objects::nonNull);
    }


    @Test
    public void givesFalseIfAnyElementFailsPredicate() {
        all(words, x -> x.length() == 0);
    }


    @Test
    public void givesTrueIfCollectionIsEmpty() {
        all(empty, x -> x.length() == 0);
    }

}
