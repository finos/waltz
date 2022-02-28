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

import java.util.Optional;

import static java.util.function.Function.identity;
import static org.junit.jupiter.api.Assertions.*;

public class StringUtilities_isEmptyTest {

    @Test
    public void trueIfOnlyWhitespace() {
        assertTrue(StringUtilities.isEmpty("   \t"));
    }

    @Test
    public void trueIfEmptyString() {
        assertTrue(StringUtilities.isEmpty(""));
    }

    @Test
    public void trueIfNull() {
        assertTrue(StringUtilities.isEmpty((String) null));
    }

    @Test
    public void falseIfNotEmpty() {
        assertFalse(StringUtilities.isEmpty("  hello  "));
    }

    @Test
    public void optionalEmptyStringTab(){
        assertTrue(StringUtilities.isEmpty(Optional.of("   \t")));
    }

    @Test
    public void optionalEmptyStringWhiteSpace(){
        assertTrue(StringUtilities.isEmpty(Optional.of("")));
    }

    @Test
    public void optionalEmptyTrueIfNull() {
        assertThrows(NullPointerException.class,
                () -> StringUtilities.isEmpty(Optional.of(null)));
    }

    @Test
    public void optionalEmptyFalseIfNotEmpty() {
        assertFalse(StringUtilities.isEmpty(Optional.of("  hello  ")));
    }
}
