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

import org.finos.waltz.model.aggregate_overlay_diagram.AggregateOverlayDiagramCallout;
import org.finos.waltz.model.aggregate_overlay_diagram.DiagramCalloutCreateCommand;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.service.aggregate_overlay_diagram.AggregateOverlayDiagramCalloutService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class AggregateOverlayDiagramCalloutEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(AggregateOverlayDiagramCalloutEndpoint.class);
    private static final String BASE_URL = WebUtilities.mkPath("api", "aggregate-overlay-diagram-callout");

    private final AggregateOverlayDiagramCalloutService aggregateOverlayDiagramCalloutService;
    private final UserRoleService userRoleService;


    @Autowired
    public AggregateOverlayDiagramCalloutEndpoint(AggregateOverlayDiagramCalloutService aggregateOverlayDiagramCalloutService,
                                                  UserRoleService userRoleService) {
        checkNotNull(aggregateOverlayDiagramCalloutService, "aggregateOverlayDiagramCalloutService must not be null");
        checkNotNull(userRoleService, "userRoleService must not be null");

        this.aggregateOverlayDiagramCalloutService = aggregateOverlayDiagramCalloutService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {

        String findByDiagramInstanceIdPath = mkPath(BASE_URL, "diagram-instance-id", ":id");
        String createPath = mkPath(BASE_URL, "create");
        String updatePath = mkPath(BASE_URL, "update");
        String deletePath = mkPath(BASE_URL, "remove", "id", ":id");

        ListRoute<AggregateOverlayDiagramCallout> findByDiagramInstanceIdRoute = (request, response) -> {
            return aggregateOverlayDiagramCalloutService.findByDiagramInstanceId(getId(request));
        };

        DatumRoute<Integer> createRoute = (request, response) -> {
            ensureUserHasEditRights(request);
            return aggregateOverlayDiagramCalloutService.create(readBody(request, DiagramCalloutCreateCommand.class));
        };


        DatumRoute<Integer> updateRoute = (request, response) -> {
            ensureUserHasEditRights(request);
            return aggregateOverlayDiagramCalloutService.update(readBody(request, AggregateOverlayDiagramCallout.class));
        };


        DatumRoute<Integer> deleteRoute = (request, response) -> {
            ensureUserHasEditRights(request);
            return aggregateOverlayDiagramCalloutService.delete(getId(request));
        };

        getForList(findByDiagramInstanceIdPath, findByDiagramInstanceIdRoute);
        postForDatum(createPath, createRoute);
        postForDatum(updatePath, updateRoute);
        deleteForDatum(deletePath, deleteRoute);
    }

    private void ensureUserHasEditRights(Request request) {
        requireRole(userRoleService, request, SystemRole.AGGREGATE_OVERLAY_DIAGRAM_EDITOR);
    }

}
