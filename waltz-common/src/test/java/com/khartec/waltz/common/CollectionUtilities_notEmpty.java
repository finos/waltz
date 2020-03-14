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

import static com.khartec.waltz.common.CollectionUtilities.notEmpty;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class CollectionUtilities_notEmpty {


    @Test
    public void nullCollectionIsEmpty() {
        assertFalse(notEmpty(null));
    }


    @Test
    public void emptyCollectionIsEmpty() {
        assertFalse(notEmpty(Collections.emptyList()));
    }


    @Test
    public void collectionWithAnythingInItIsNotEmpty() {
        assertTrue(notEmpty(SetUtilities.asSet("A")));
    }

}
