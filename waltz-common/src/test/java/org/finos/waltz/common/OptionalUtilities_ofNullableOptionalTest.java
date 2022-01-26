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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OptionalUtilities_ofNullableOptionalTest {

    @Test
    public void givingNullReturnsEmpty() {
        assertEquals(
                Optional.empty(),
                OptionalUtilities.ofNullableOptional(null));
    }


    @Test
    public void givingEmptyReturnsEmpty() {
        assertEquals(
                Optional.empty(),
                OptionalUtilities.ofNullableOptional(Optional.empty()));
    }


    @Test
    public void givingSomethingReturnsTheSameSomething() {
        assertEquals(
                Optional.of("hello"),
                OptionalUtilities.ofNullableOptional(Optional.of("hello")));
    }

}