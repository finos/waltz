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

package com.khartec.waltz.web.endpoints.extracts;

import spark.Request;
import spark.Response;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class BinaryDataBasedDataExtractor implements DataExtractor {

    protected Object writeExtract(String suggestedFilenameStem,
                                  byte[] dataBytes,
                                  Request request,
                                  Response response) throws IOException {
        ExtractFormat format = parseExtractFormat(request);
        switch (format) {
            case SVG:
                return writeSvg(suggestedFilenameStem, dataBytes, response);
            default:
                throw new IllegalArgumentException("Cannot write extract using format: " + format);
        }
    }


    private Object writeSvg(String suggestedFilenameStem,
                            byte[] dataBytes,
                            Response response) throws IOException {
        HttpServletResponse httpResponse = response.raw();

        httpResponse.setHeader("Content-Type", "image/svg+xml");
        httpResponse.setHeader("Content-Disposition", "attachment; filename=" + suggestedFilenameStem + ".svg");
        httpResponse.setHeader("Content-Transfer-Encoding", "7bit");

        httpResponse.setContentLength(dataBytes.length);
        httpResponse.getOutputStream().write(dataBytes);
        httpResponse.getOutputStream().flush();
        httpResponse.getOutputStream().close();

        return httpResponse;
    }
}
