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

package com.khartec.waltz.web.endpoints;

import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.WebUtilities;
import spark.ResponseTransformer;
import spark.Route;
import spark.Spark;

import static com.khartec.waltz.web.WebUtilities.TYPE_JSON;


public class EndpointUtilities {

    private static final ResponseTransformer transformer = WebUtilities.transformer;


    /**
     * Helper method to register a route which provides a list of items.
     * This helps comprehension as the return types of routes becomes explicit.
     * @param path
     * @param handler
     * @param <T>
     */
    public static <T> void getForList(String path, ListRoute<T> handler) {
        Spark.get(path, wrapListHandler(handler), transformer);
    }


    /**
     * Helper method to register a route which provides a single item (not
     * a list).  This helps comprehension as the return types of routes
     * becomes explicit.
     * @param path
     * @param handler
     * @param <T>
     */
    public static <T> void getForDatum(String path, DatumRoute<T> handler) {
        Spark.get(path, wrapDatumHandler(handler), transformer);
    }

    public static <T> void postForDatum(String path, DatumRoute<T> handler) {
        Spark.post(path, wrapDatumHandler(handler), transformer);
    }

    public static <T> void postForList(String path, ListRoute<T> handler) {
        Spark.post(path, wrapListHandler(handler), transformer);
    }

    public static <T> void deleteForList(String path, ListRoute<T> handler) {
        Spark.delete(path, wrapListHandler(handler), transformer);
    }

    public static <T> void deleteForDatum(String path, DatumRoute<T> handler) {
        Spark.delete(path, wrapDatumHandler(handler), transformer);
    }

    public static <T> void putForDatum(String path, DatumRoute<T> handler) {
        Spark.put(path, wrapDatumHandler(handler), transformer);
    }

    public static <T> void putForList(String path, ListRoute<T> handler) {
        Spark.put(path, wrapListHandler(handler), transformer);
    }


    // -- helpers ---

    private static <T> Route wrapListHandler(ListRoute<T> handler) {
        return (request, response) -> {
            response.type(TYPE_JSON);
            return handler.apply(request, response);
        };
    }

    private static <T> Route wrapDatumHandler(DatumRoute<T> handler) {
        return (request, response) -> {
            response.type(TYPE_JSON);
            return handler.apply(request, response);
        };
    }

}
