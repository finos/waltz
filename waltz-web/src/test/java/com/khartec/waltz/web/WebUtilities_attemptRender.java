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

package com.khartec.waltz.web;

import org.junit.Test;
import org.slf4j.LoggerFactory;
import spark.RequestResponseFactory;
import spark.Response;

import javax.servlet.http.HttpServletResponse;

public class WebUtilities_attemptRender {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(WebUtilities_attemptRender.class);

    private Response resp = RequestResponseFactory.create((HttpServletResponse) null);


    @Test(expected = IllegalArgumentException.class)
    public void mustProvideResponseObj() {
        WebUtilities.attemptRender(null, "foo", LOG);
    }


    @Test(expected = IllegalArgumentException.class)
    public void mustProvideObjToRender() {
        WebUtilities.attemptRender(resp, null, LOG);
    }


    @Test(expected = IllegalArgumentException.class)
    public void mustProvideLogger() {
        WebUtilities.attemptRender(resp, "foo", null);
    }

}
