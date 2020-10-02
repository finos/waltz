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

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.report_grid.ReportGrid;
import com.khartec.waltz.service.report_grid.ReportGridService;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForDatum;

@Service
public class ReportGridEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "report-grid");

    private final ReportGridService reportGridService;


    @Autowired
    public ReportGridEndpoint(ReportGridService reportGridService) {
        this.reportGridService = reportGridService;
    }


    @Override
    public void register() {
        String findAllPath = mkPath(BASE_URL, "all");
        String getViewByIdPath = mkPath(BASE_URL, "view", "id", ":id");
//        String findByExtIdPath = mkPath(BASE_URL, "view", "external-id", ":externalId");


        getForDatum(findAllPath, (req, resp) -> reportGridService.findAll());
        postForDatum(getViewByIdPath, this::getViewByIdRoute);
//        postForDatum(findByExtIdPath, this::getByExtIdRoute);
    }


    public ReportGrid getViewByIdRoute(Request req, Response resp) throws IOException {
        return reportGridService.getByIdAndSelectionOptions(
                getId(req),
                readIdSelectionOptionsFromBody(req));
    }


}
