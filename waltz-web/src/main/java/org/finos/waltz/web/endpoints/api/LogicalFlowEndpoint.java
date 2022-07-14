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

import org.finos.waltz.model.Operation;
import org.finos.waltz.service.logical_flow.LogicalFlowService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.logical_flow.AddLogicalFlowCommand;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.logical_flow.LogicalFlowGraphSummary;
import org.finos.waltz.model.logical_flow.LogicalFlowStatistics;
import org.finos.waltz.model.user.SystemRole;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.ListUtilities.map;
import static org.finos.waltz.common.ListUtilities.newArrayList;


@Service
public class LogicalFlowEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(LogicalFlowEndpoint.class);
    private static final String BASE_URL = mkPath("api", "logical-flow");

    private final LogicalFlowService logicalFlowService;
    private final UserRoleService userRoleService;


    @Autowired
    public LogicalFlowEndpoint(LogicalFlowService logicalFlowService,
                               UserRoleService userRoleService) {
        checkNotNull(logicalFlowService, "logicalFlowService must not be null");
        checkNotNull(userRoleService, "userRoleService must not be null");

        this.logicalFlowService = logicalFlowService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String findByEntityPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String findBySelectorPath = mkPath(BASE_URL, "selector");
        String findByIdsPath = mkPath(BASE_URL, "ids");
        String findBySourceAndTargetsPath = mkPath(BASE_URL, "source-targets");
        String findStatsPath = mkPath(BASE_URL, "stats");
        String findFlowPermissionsPath = mkPath(BASE_URL, "entity", ":kind", ":id", "permissions");
        String findUpstreamFlowsForEntityReferencesPath = mkPath(BASE_URL, "find-upstream-flows");
        String getByIdPath = mkPath(BASE_URL, ":id");
        String removeFlowPath = mkPath(BASE_URL, ":id");
        String cleanupOrphansPath = mkPath(BASE_URL, "cleanup-orphans");
        String cleanupSelfReferencesPath = mkPath(BASE_URL, "cleanup-self-references");
        String addFlowPath = mkPath(BASE_URL);
        String addFlowsPath = mkPath(BASE_URL, "list");
        String getFlowGraphSummaryPath = mkPath(BASE_URL, "entity", ":kind", ":id", "data-type", ":dtId", "graph-summary");

        ListRoute<LogicalFlow> getByEntityRef = (request, response)
                -> logicalFlowService.findByEntityReference(getEntityReference(request));

        ListRoute<LogicalFlow> findBySelectorRoute = (request, response)
                -> logicalFlowService.findBySelector(readIdSelectionOptionsFromBody(request));

        ListRoute<LogicalFlow> findByIdsRoute = (request, response)
                -> logicalFlowService.findActiveByFlowIds(readIdsFromBody(request));

        ListRoute<LogicalFlow> findUpstreamFlowsForEntityReferencesRoute = (request, response) -> {
            EntityReference[] refs = readBody(request, EntityReference[].class);
            return logicalFlowService.findUpstreamFlowsForEntityReferences(newArrayList(refs));
        };

        ListRoute<Operation> findFlowPermissionsRoute = (request, response) -> logicalFlowService.findFlowPermissionsForParentEntity(
                getEntityReference(request),
                getUsername(request));

        DatumRoute<LogicalFlowStatistics> findStatsRoute = (request, response)
                -> logicalFlowService.calculateStats(readIdSelectionOptionsFromBody(request));

        DatumRoute<LogicalFlow> getByIdRoute = (request, response)
                -> logicalFlowService.getById(getId(request));

        DatumRoute<LogicalFlowGraphSummary> getGraphSummaryRoute = (request, response) -> {
            String dtIdString = request.params("dtId");
            Long dtId = StringUtilities.parseLong(dtIdString, null);
            return logicalFlowService.getFlowInfoByDirection(getEntityReference(request), dtId);
        };

        getForDatum(cleanupOrphansPath, this::cleanupOrphansRoute);
        getForDatum(cleanupSelfReferencesPath, this::cleanupSelfReferencingFlowsRoute);
        getForList(findByEntityPath, getByEntityRef);
        getForList(findFlowPermissionsPath, findFlowPermissionsRoute);
        getForDatum(getByIdPath, getByIdRoute);
        getForDatum(getFlowGraphSummaryPath, getGraphSummaryRoute);
        postForList(findByIdsPath, findByIdsRoute);
        postForList(findUpstreamFlowsForEntityReferencesPath, findUpstreamFlowsForEntityReferencesRoute);
        postForList(findBySelectorPath, findBySelectorRoute);
        postForDatum(findBySourceAndTargetsPath, this::findBySourceAndTargetsRoute);
        postForDatum(findStatsPath, findStatsRoute);
        deleteForDatum(removeFlowPath, this::removeFlowRoute);
        postForDatum(addFlowPath, this::addFlowRoute);
        postForList(addFlowsPath, this::addFlowsRoute);
    }


    private Integer cleanupOrphansRoute(Request request, Response response) {
        ensureUserHasAdminRights(request);

        String username = getUsername(request);

        LOG.info("User: {}, requested logical flow cleanup", username);
        return logicalFlowService.cleanupOrphans();
    }


    private int cleanupSelfReferencingFlowsRoute(Request request, Response response) {
        ensureUserHasAdminRights(request);

        String username = getUsername(request);

        LOG.info("User: {}, requested self referencing logical flow cleanup", username);
        return logicalFlowService.cleanupSelfReferencingFlows();
    }


    private List<LogicalFlow> findBySourceAndTargetsRoute(Request request, Response response) throws IOException {
        List list = readBody(request, List.class);

        @SuppressWarnings("unchecked")
        List<Tuple2<EntityReference, EntityReference>> sourcesAndTargets = map(
                list,
                t -> {
                    Map map = (Map) t;
                    Map source = (Map) map.get("source");
                    Map target = (Map) map.get("target");
                    EntityReference sourceRef = EntityReference.mkRef(source);
                    EntityReference targetRef = EntityReference.mkRef(target);
                    return Tuple.tuple(sourceRef, targetRef);
                });

        return logicalFlowService
                .findBySourceAndTargetEntityReferences(sourcesAndTargets);
    }


    private LogicalFlow addFlowRoute(Request request, Response response) throws IOException {
        ensureUserHasEditRights(request);

        String username = getUsername(request);

        AddLogicalFlowCommand addCmd = readBody(request, AddLogicalFlowCommand.class);


        LOG.info("User: {}, adding new logical flow: {}", username, addCmd);
        return logicalFlowService.addFlow(addCmd, username);
    }


    private List<LogicalFlow> addFlowsRoute(Request request, Response response) throws IOException {
        ensureUserHasEditRights(request);

        String username = getUsername(request);

        List<AddLogicalFlowCommand> addCmds = Arrays.asList(readBody(request, AddLogicalFlowCommand[].class));
        LOG.info("User: {}, adding new logical flows: {}", username, addCmds);
        return logicalFlowService.addFlows(addCmds, username);
    }


    private int removeFlowRoute(Request request, Response response) {
        ensureUserHasEditRights(request);

        long flowId = getId(request);
        String username = getUsername(request);

        LOG.info("User: {} removing logical flow: {}", username, flowId);

        return logicalFlowService.removeFlow(flowId, username);
    }


    private void ensureUserHasEditRights(Request request) {
        requireRole(userRoleService, request, SystemRole.LOGICAL_DATA_FLOW_EDITOR);
    }


    private void ensureUserHasAdminRights(Request request) {
        requireRole(userRoleService, request, SystemRole.ADMIN);
    }
}
