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

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.web.endpoints.Endpoint;
import org.eclipse.jetty.http.HttpHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static com.khartec.waltz.common.IOUtilities.copyStream;
import static com.khartec.waltz.web.WebUtilities.getMimeType;
import static java.lang.String.format;

public class StaticResourcesEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(StaticResourcesEndpoint.class);
    private static final String CACHE_MAX_AGE_VALUE = "max-age=" + TimeUnit.DAYS.toSeconds(30);


    private final ClassLoader classLoader = StaticResourcesEndpoint.class
            .getClassLoader();


    @Override
    public void register() {
        LOG.debug("Registering static resources");

        Spark.get("/*", (request, response) -> {

            String resolvedPath = resolvePath(request);

            if (resolvedPath == null) {
                return null;
            }

            try (
                InputStream resourceAsStream = classLoader.getResourceAsStream(resolvedPath)
            ) {
                if (resourceAsStream == null) {
                    return null;
                } else {
                    String message = format(
                            "Serving %s in response to request for %s",
                            resolvedPath,
                            request.pathInfo());
                    LOG.info(message);

                    response.type(getMimeType(resolvedPath));

                    addCacheHeadersIfNeeded(response, resolvedPath);
                    OutputStream out = response
                            .raw()
                            .getOutputStream();
                    copyStream(resourceAsStream, out);
                    out.flush();

                    return new Object(); // indicate we have handled the request
                }
            } catch (Exception e) {
                LOG.warn("Encountered error when attempting to serve: "+resolvedPath, e);
                return null;
            }
        });
    }


    /**
     * We want to add a cache-control: max-age value to all resources except html.
     * This is because the html resources have references to 'cache-busted' js files
     * and other resources.  If the html was also cached then it would be difficult
     * to detect client code updates.
     *
     * @param response - the http response we are servicing
     * @param resolvedPath - the resolved path to the resource we are serving
     */
    private void addCacheHeadersIfNeeded(Response response, String resolvedPath) {
        if (! resolvedPath.endsWith(".html")) {
            response.header(HttpHeader.CACHE_CONTROL.toString(), CACHE_MAX_AGE_VALUE);
        }
    }


    private String resolvePath(Request request) {
        String path = request.pathInfo().replaceFirst("/", "");
        String resourcePath = path.length() > 0 ? ("static/" + path) : "static/index.html";

        URL resource = classLoader.getResource(resourcePath);

        if (resource == null) {
            return null;
        }

        boolean isDirectory = resource
                .getPath()
                .endsWith("/");

        return isDirectory
                ? resourcePath + "/index.html"
                : resourcePath;
    }

}
