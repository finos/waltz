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

package org.finos.waltz.web.endpoints.api;

import org.finos.waltz.web.endpoints.Endpoint;
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

import static org.finos.waltz.web.WebUtilities.getMimeType;
import static java.lang.String.format;
import static org.finos.waltz.common.IOUtilities.copyStream;
import static org.finos.waltz.common.IOUtilities.readLines;
import static org.finos.waltz.common.StringUtilities.lower;
import static org.finos.waltz.common.StringUtilities.notEmpty;

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
        if (! resolvedPath.endsWith(".html")) {
            response.header(HttpHeader.CACHE_CONTROL.toString(), CACHE_MAX_AGE_VALUE);
        }
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
                    line = line.replaceFirst(
                            "<base href=(['\"])/(['\"])\\s*/>",
                            format(
                                "\t<base href=\"%s/\" />",
                                request.contextPath()));
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
                return new ByteArrayInputStream(outputStream.toByteArray());
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
            // 404: return index.html
            resource = classLoader.getResource(indexPath);
            resourcePath = indexPath;
        }

        boolean isDirectory = resource
                .getPath()
                .endsWith("/");

        return isDirectory
                ? resourcePath + "/index.html"
                : resourcePath;
    }

}
