/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
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


    private static <T> Route wrapListHandler(ListRoute<T> handler) {
        return (request, response) -> {
            response.type(TYPE_JSON);
            return handler.apply(request, response);
        };
    }

    public static <T> void getForList(String path, ListRoute<T> handler) {
        Spark.get(path, wrapListHandler(handler), transformer);
    }

    public static <T> void post(String path, Route handler) {
        Spark.post(path, handler, transformer);
    }

    public static <T> void postForList(String path, ListRoute<T> handler) {
        Spark.post(path, wrapListHandler(handler), transformer);
    }

    public static <T> void postForDatum(String path, DatumRoute<T> handler) {
        Spark.post(path, (request, response) -> {
            response.type(TYPE_JSON);
            return handler.apply(request, response);
        }, transformer);
    }



    public static <T> void getForDatum(String path, DatumRoute<T> handler) {
        Spark.get(path, (request, response) -> {
            response.type(TYPE_JSON);
            return handler.apply(request, response);
        }, transformer);
    }


}
