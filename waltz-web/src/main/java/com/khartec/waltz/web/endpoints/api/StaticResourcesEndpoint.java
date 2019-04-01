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

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.khartec.waltz.common.IOUtilities.copyStream;
import static com.khartec.waltz.common.IOUtilities.readLines;
import static com.khartec.waltz.common.StringUtilities.lower;
import static com.khartec.waltz.common.StringUtilities.notEmpty;
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
                    InputStream modifiedStream = modifyIndexBaseTagIfNeeded(request, resolvedPath, resourceAsStream);

                    OutputStream out = response
                            .raw()
                            .getOutputStream();
                    copyStream(modifiedStream, out);
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
        if (!isHtml(resolvedPath)) {
            response.header(HttpHeader.CACHE_CONTROL.toString(), CACHE_MAX_AGE_VALUE);
        }
    }


    private boolean isHtml(String path) {
        return lower(path).endsWith(".html");
    }


    /**
     * index.html need to have a <base href="/[site_context]/" /> tag in the head section to ensure
     * html5 mode works correctly in AngularJS.  This method will ensure the existing <base href="/" /> tag
     * is replace with one that includes the correct site context as deployed.
     * @param request
     * @param resolvedPath
     * @param resourceStream
     * @return the modified input stream with the amended <base> tag or the original unmodified resourceStream
     * @throws IOException
     */
    private InputStream modifyIndexBaseTagIfNeeded(Request request,
                                                   String resolvedPath,
                                                   InputStream resourceStream) throws IOException {
        if(resolvedPath.endsWith("index.html") && notEmpty(request.contextPath())) {
            List<String> lines = readLines(resourceStream);

            for(int i = 0; i < lines.size(); i++) {
                String line = lower(lines.get(i));

                if (line.contains("<base href=")) {
                    LOG.info("Found <base> tag: " + line + ", adding context path: " + request.contextPath());
                    line = String.format("\t<base href=\"%s/\" />", request.contextPath());
                    LOG.info("Updated <base> tag: " + line);
                    lines.set(i, line);

                    // done, exit loop
                    break;
                }

                if (line.contains("</head>") ) {
                    // don't need to continue if have reached here and no base tag found
                    break;
                }
            }

            // copy amended file into a stream
            try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                 OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {

                for(String line : lines) {
                    writer.write(line);
                    writer.write(System.lineSeparator());
                }

                writer.flush();
                ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                return inputStream;
            }
        }
        return resourceStream;
    }


    private String resolvePath(Request request) {
        final String indexPath = "static/index.html";
        String path = request.pathInfo().replaceFirst("/", "");
        String resourcePath = path.length() > 0 ? ("static/" + path) : indexPath;

        URL resource = classLoader.getResource(resourcePath);

        if (resource == null) {
            return isHtml(path)
                ? indexPath
                : null;
        } else {
            String resolvedPath = resource
                    .getPath();

            boolean isDirectory = resolvedPath
                    .endsWith("/");

            return isDirectory
                    ? resolvedPath + "/index.html"
                    : resolvedPath;
        }
    }

}
