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

import org.finos.waltz.service.report_grid.ReportGridFilterViewService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.web.WebUtilities.getId;
import static org.finos.waltz.web.WebUtilities.mkPath;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForDatum;

@Service
public class ReportGridViewEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "report-grid-view");

    private final ReportGridFilterViewService reportGridFilterViewService;


    @Autowired
    public ReportGridViewEndpoint(ReportGridFilterViewService reportGridFilterViewService) {
        this.reportGridFilterViewService = reportGridFilterViewService;
    }


    @Override
    public void register() {
        String recalculateAppGroupPath = mkPath(BASE_URL, "recalculate", "app-group-id", ":id");

        DatumRoute<Boolean> recalculateAppGroupRoute = (req, resp) -> {
            long appGroupId = getId(req);
            return reportGridFilterViewService.recalculateAppGroupFromNoteText(appGroupId);
        };
        getForDatum(recalculateAppGroupPath, recalculateAppGroupRoute);
    }

}
