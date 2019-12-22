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

public class Checks_checkAll {

    @Test(expected = IllegalArgumentException.class)
    public void mustBeGivenAnArray() {
        Checks.checkAll((Object[]) null, x -> true, "test");
    }

    
    @Test(expected = IllegalArgumentException.class)
    public void mustBeGivenACollection() {
        Checks.checkAll((Collection<?>) null, x -> true, "test");
    }

    
    @Test(expected = IllegalArgumentException.class)
    public void mustBeGivenAPredicate() {
        Checks.checkAll(new String[]{}, null, "test");
    }


    @Test
    public void passesIfPredicateIsTrueForAll() {
        Checks.checkAll(new Integer[]{1, 3, 5}, x -> (x.intValue() % 2) == 1, "test");
    }

    
    @Test(expected = IllegalArgumentException.class)
    public void throwsIfPredicateIsFalseForAny() {
        Checks.checkAll(new Integer[]{1, 4, 5}, x -> (x.intValue() % 2) == 1, "test");
    }
}
