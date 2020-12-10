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

import org.eclipse.jetty.http.MimeTypes;
import org.jooq.lambda.tuple.Tuple3;
import spark.Request;
import spark.Response;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.khartec.waltz.common.EnumUtilities.readEnum;
import static com.khartec.waltz.common.StringUtilities.mkSafe;

/**
 * Represents classes which are used for Data Extraction.
 */
public interface DataExtractor {

    void register();


    /**
     * Reads the extract format from the web request.  Expects as a param with the
     * name `format`.  E.g. `my-extract?format=XLSX`
     *
     * Defaults to` ExtractFormat.CSV` if not found or not recognized.
     *
     * @param request  web request
     * @return enum representing the value of the format parameter
     */
    default ExtractFormat parseExtractFormat(Request request) {
        return readEnum(
                request.queryParams("format"),
                ExtractFormat.class,
                v -> ExtractFormat.CSV);
    }


    /**
     * Removes illegal characters from sheetName.  Currently: `\ : ; * ? / `
     *
     * @param name  input name of the sheet
     * @return outputs sanitized sheet name
     */
    default String sanitizeSheetName(String name) {
        return mkSafe(name).replaceAll("[:;*?/\\\\]", "");
    }


    default Object writeReportResults(Response response, Tuple3<ExtractFormat, String, byte[]> reportResult) throws IOException {
        String templateName = reportResult.v2;

        HttpServletResponse httpResponse = response.raw();

        switch (reportResult.v1) {
            case XLSX:
                httpResponse.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                httpResponse.setHeader("Content-Disposition", "attachment; filename=" + templateName + ".xlsx");
                httpResponse.setHeader("Content-Transfer-Encoding", "7bit");
                break;
            case CSV:
                response.type(MimeTypes.Type.TEXT_PLAIN.name());
                response.header("Content-disposition", "attachment; filename=" + templateName + ".csv");
                break;
            default:
                break;
        }

        byte[] bytes = reportResult.v3;
        httpResponse.setContentLength(bytes.length);
        httpResponse.getOutputStream().write(bytes);
        httpResponse.getOutputStream().flush();
        httpResponse.getOutputStream().close();

        return httpResponse;
    }


}
