/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
