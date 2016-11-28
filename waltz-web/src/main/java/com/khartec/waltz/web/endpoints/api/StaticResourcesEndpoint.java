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

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Spark;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import static com.khartec.waltz.common.IOUtilities.copyStream;
import static com.khartec.waltz.web.WebUtilities.getMimeType;
import static java.lang.String.format;

public class StaticResourcesEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(StaticResourcesEndpoint.class);

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
                InputStream resourceAsStream = classLoader.getResourceAsStream(resolvedPath);
            ) {
                if (resourceAsStream == null) {
                    return null;
                } else {
                    String message = format("Serving %s in response to request for %s", resolvedPath, request.pathInfo());
                    LOG.info(message);

                    response.type(getMimeType(resolvedPath));
                    response.header("Cache-Control", "max-age=2592000"); //30 day cache
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


    private String resolvePath(Request request) {
        String path = request.pathInfo().replaceFirst("\\/", "");
        String resourcePath = path.length() > 0 ? ("static/" + path) : "static/index.html";

        URL resource = classLoader.getResource(resourcePath);

        if (resource == null) {
            return null;
        }

        boolean isDirectory = resource
                .getPath()
                .endsWith("/");

        String resolvedPath = isDirectory
                ? resourcePath + "/index.html"
                : resourcePath;

        return resolvedPath;
    }

}
