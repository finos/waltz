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

import static com.khartec.waltz.common.StringUtilities.firstChar;
import static com.khartec.waltz.common.StringUtilities.mkPath;
import static org.junit.Assert.assertEquals;

public class StringUtilities_mkPath {

    @Test
    public void mkPathWithNoSegmentsReturnsEmptyString() {
        assertEquals("", mkPath());
    }

    @Test
    public void mkPathWithSegmentsReturnsSlashDelimitedConcatenation() {
        assertEquals("hello/world", mkPath("hello", "world"));
    }


    @Test
    public void mkPathWithNonStringSegmentsReturnsToStringEquivalent() {
        assertEquals("hello/23/world", mkPath("hello", 23, "world"));
    }


    @Test
    public void mkPathWithOneSegmentsReturnsBasicString() {
        assertEquals("test", mkPath("test"));
    }


    @Test
    public void mkPathWithEmbeddedSegmentsPreservesSlashes() {
        assertEquals("test/one", mkPath("test/one"));
    }


    @Test
    public void mkPathWithEmbeddedSegmentsReducesSlashes() {
        assertEquals("test/one", mkPath("test///one"));
    }


    @Test(expected = IllegalArgumentException.class)
    public void mkPathWithNullSegmentsIsNotAllowed() {
        assertEquals("test", mkPath("test", null));
    }


    @Test(expected = IllegalArgumentException.class)
    public void mkPathWithEmptySegmentsIsNotAllowed() {
        assertEquals("test", mkPath("test", ""));
    }


}
