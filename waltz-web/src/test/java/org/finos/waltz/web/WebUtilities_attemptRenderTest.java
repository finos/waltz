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
import org.slf4j.LoggerFactory;
import spark.RequestResponseFactory;
import spark.Response;

import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class WebUtilities_attemptRenderTest {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(WebUtilities_attemptRenderTest.class);

    private Response resp = RequestResponseFactory.create((HttpServletResponse) null);


    @Test
    public void mustProvideResponseObj() {
        assertThrows(IllegalArgumentException.class,
                () -> WebUtilities.attemptRender(null, "foo", LOG));
    }


    @Test
    public void mustProvideObjToRender() {
        assertThrows(IllegalArgumentException.class,
                () -> WebUtilities.attemptRender(resp, null, LOG));
    }


    @Test
    public void mustProvideLogger() {
        assertThrows(IllegalArgumentException.class,
                () -> WebUtilities.attemptRender(resp, "foo", null));
    }

}
