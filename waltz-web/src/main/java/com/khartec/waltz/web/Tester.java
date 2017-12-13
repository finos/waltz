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

import spark.Request;
import spark.Response;
import spark.Spark;

import java.io.IOException;
import java.util.Map;

import static com.khartec.waltz.web.WebUtilities.readBody;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForDatum;


public class Tester {
    public static void main(String[] args) {
        postForDatum("/", Tester::handle);
        Spark.init();
    }

    private static String handle(Request request, Response response)  {
        try {
            Map m = readBody(request, Map.class);
            System.out.println(m);
            return "Hi " + m.get("description");
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }

    }
}
