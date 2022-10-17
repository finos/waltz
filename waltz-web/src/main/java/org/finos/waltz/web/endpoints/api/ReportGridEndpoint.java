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

import org.finos.waltz.common.FunctionUtilities;
import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.common.exception.NotFoundException;
import org.finos.waltz.model.report_grid.*;
import org.finos.waltz.service.report_grid.ReportGridService;
import org.finos.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Set;

import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;

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
        String findForUserPath = mkPath(BASE_URL, "user");
        String createPath = mkPath(BASE_URL, "create");
        String updatePath = mkPath(BASE_URL, "id", ":id", "update");
        String removalPath = mkPath(BASE_URL, "id", ":id");
        String findForOwnerPath = mkPath(BASE_URL, "owner");
        String getViewByIdPath = mkPath(BASE_URL, "view", "id", ":id");
        String updateColumnDefsPath = mkPath(BASE_URL, "id", ":id", "column-definitions", "update");

        getForDatum(findAllPath, (req, resp) -> reportGridService.findAll());
        getForList(findForUserPath, (req, resp) -> reportGridService.findForUser(getUsername(req)));
        getForList(findForOwnerPath, this::findForOwnerRoute);
        postForDatum(getViewByIdPath, this::getViewByIdRoute);
        postForDatum(updateColumnDefsPath, this::updateColumnDefsRoute);
        postForDatum(createPath, this::createRoute);
        postForDatum(updatePath, this::updateRoute);
        deleteForDatum(removalPath, this::removalRoute);
    }


    private boolean removalRoute(Request request,
                                 Response response) throws InsufficientPrivelegeException {
        return reportGridService.remove(
                getId(request),
                getUsername(request));
    }


    public ReportGrid getViewByIdRoute(Request req,
                                       Response resp) throws IOException {
        return reportGridService
                .getByIdAndSelectionOptions(getId(req), readIdSelectionOptionsFromBody(req))
                .orElseThrow(() -> new NotFoundException("404", "ID not found"));
    }


    public ReportGridDefinition updateColumnDefsRoute(Request req,
                                                      Response resp) throws IOException, InsufficientPrivelegeException {
        return reportGridService.updateColumnDefinitions(
                getId(req),
                readBody(req, ReportGridColumnDefinitionsUpdateCommand.class),
                getUsername(req));
    }


    public ReportGridDefinition createRoute(Request req,
                                            Response resp) throws IOException {
        return reportGridService.create(readBody(req, ReportGridCreateCommand.class), getUsername(req));
    }


    public ReportGridDefinition updateRoute(Request req,
                                            Response resp) throws IOException, InsufficientPrivelegeException {
        return reportGridService.
                update(getId(req), readBody(req, ReportGridUpdateCommand.class), getUsername(req));
    }


    public Set<ReportGridDefinition> findForOwnerRoute(Request req,
                                                       Response resp) {
        return reportGridService.findForOwner(getUsername(req));
    }
}
