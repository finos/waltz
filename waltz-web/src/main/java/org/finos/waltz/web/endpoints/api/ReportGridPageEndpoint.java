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
import org.finos.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.ModelAndView;
import spark.TemplateViewRoute;
import spark.template.freemarker.FreeMarkerEngine;

import java.util.HashMap;
import java.util.Map;

import static org.finos.waltz.web.WebUtilities.getId;
import static org.finos.waltz.web.WebUtilities.mkPath;
import static spark.Spark.get;

@Service
public class ReportGridPageEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("page", "report-grid-view");

    private final ReportGridFilterViewService reportGridFilterViewService;


    @Autowired
    public ReportGridPageEndpoint(ReportGridFilterViewService reportGridFilterViewService) {
        this.reportGridFilterViewService = reportGridFilterViewService;
    }


    @Override
    public void register() {
        String recalculateAppGroupPath = mkPath(BASE_URL, "recalculate", "app-group-id", ":id");

        TemplateViewRoute templateViewRoute = (req, resp) -> {
            long appGroupId = getId(req);

            Map<String, Object> model = new HashMap<>();
            model.put("appGroupId", String.valueOf(appGroupId));

            try {
                int appCount = reportGridFilterViewService.recalculateAppGroupFromNoteText(appGroupId);
                model.put("success", true);
                model.put("appCount", appCount);
            } catch (IllegalArgumentException e) {
                model.put("success", false);
                model.put("errorMessage", e.getMessage());
            }

            return new ModelAndView(model, "recalculate-app-group-page.ftl"); // located in src/main/resources/spark/template/freemarker
        };

        get(recalculateAppGroupPath, templateViewRoute, new FreeMarkerEngine());
    }

}
