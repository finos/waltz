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

package org.finos.waltz.web;

import org.junit.jupiter.api.Test;
import spark.Request;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebUtilities_getLocalDateQueryParamTest {

    private Request mkRequest(String paramName, String value) {
        Request request = mock(Request.class);
        when(request.queryParams(paramName)).thenReturn(value);
        return request;
    }

    @Test
    public void parsesAnIsoDate() {
        assertEquals(
                Optional.of(LocalDate.of(2024, 1, 15)),
                WebUtilities.getLocalDateQueryParam(mkRequest("startDate", "2024-01-15"), "startDate"));
    }

    @Test
    public void missingParamYieldsEmpty() {
        assertEquals(
                Optional.empty(),
                WebUtilities.getLocalDateQueryParam(mkRequest("startDate", null), "startDate"));
    }

    @Test
    public void unparseableDateYieldsEmpty() {
        assertEquals(
                Optional.empty(),
                WebUtilities.getLocalDateQueryParam(mkRequest("startDate", "not-a-date"), "startDate"));
    }

    @Test
    public void wrongFormatYieldsEmpty() {
        assertEquals(
                Optional.empty(),
                WebUtilities.getLocalDateQueryParam(mkRequest("startDate", "2024/01/15"), "startDate"));
    }
}
