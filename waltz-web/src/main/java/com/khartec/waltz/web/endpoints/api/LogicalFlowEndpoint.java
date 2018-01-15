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

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.logical_flow.AddLogicalFlowCommand;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.model.logical_flow.LogicalFlowStatistics;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.logical_flow.LogicalFlowService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
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
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


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
        String findBySourceAndTargetsPath = mkPath(BASE_URL, "source-targets");
        String findStatsPath = mkPath(BASE_URL, "stats");
        String findUpstreamFlowsForEntityReferencesPath = mkPath(BASE_URL, "find-upstream-flows");
        String getByIdPath = mkPath(BASE_URL, ":id");
        String removeFlowPath = mkPath(BASE_URL, ":id");
        String cleanupOrphansPath = mkPath(BASE_URL, "cleanup-orphans");
        String cleanupSelfReferencesPath = mkPath(BASE_URL, "cleanup-self-references");
        String addFlowPath = mkPath(BASE_URL);
        String addFlowsPath = mkPath(BASE_URL, "list");

        ListRoute<LogicalFlow> getByEntityRef = (request, response)
                -> logicalFlowService.findByEntityReference(getEntityReference(request));

        ListRoute<LogicalFlow> findBySelectorRoute = (request, response)
                -> logicalFlowService.findBySelector(readIdSelectionOptionsFromBody(request));

        ListRoute<LogicalFlow> findUpstreamFlowsForEntityReferencesRoute = (request, response) -> {
            EntityReference[] refs = readBody(request, EntityReference[].class);
            return logicalFlowService.findUpstreamFlowsForEntityReferences(newArrayList(refs));
        };

        DatumRoute<LogicalFlowStatistics> findStatsRoute = (request, response)
                -> logicalFlowService.calculateStats(readIdSelectionOptionsFromBody(request));

        DatumRoute<LogicalFlow> getByIdRoute = (request, response)
                -> logicalFlowService.getById(getId(request));

        getForDatum(cleanupOrphansPath, this::cleanupOrphansRoute);
        getForDatum(cleanupSelfReferencesPath, this::cleanupSelfReferencingFlowsRoute);
        getForList(findByEntityPath, getByEntityRef);
        getForDatum(getByIdPath, getByIdRoute);
        postForList(findUpstreamFlowsForEntityReferencesPath, findUpstreamFlowsForEntityReferencesRoute);
        postForList(findBySelectorPath, findBySelectorRoute);
        postForDatum(findBySourceAndTargetsPath, this::findBySourceAndTargetsRoute);
        postForDatum(findStatsPath, findStatsRoute);
        deleteForDatum(removeFlowPath, this::removeFlowRoute);
        postForDatum(addFlowPath, this::addFlowRoute);
        postForList(addFlowsPath, this::addFlowsRoute);
    }


    private Integer cleanupOrphansRoute(Request request, Response response) throws IOException {
        ensureUserHasAdminRights(request);

        String username = getUsername(request);

        LOG.info("User: {}, requested logical flow cleanup", username);
        return logicalFlowService.cleanupOrphans();
    }


    private int cleanupSelfReferencingFlowsRoute(Request request, Response response) throws IOException {
        ensureUserHasAdminRights(request);

        String username = getUsername(request);

        LOG.info("User: {}, requested self referencing logical flow cleanup", username);
        return logicalFlowService.cleanupSelfReferencingFlows();
    }


    private List<LogicalFlow> findBySourceAndTargetsRoute(Request request, Response response) throws IOException {
        List list = readBody(request, List.class);

        List<Tuple2<EntityReference, EntityReference>> sourcesAndTargets = (List<Tuple2<EntityReference, EntityReference>>) list.stream()
                .map(t -> {
                    Map map = (Map) t;
                    Map source = (Map) map.get("source");
                    Map target = (Map) map.get("target");
                    EntityReference sourceRef = EntityReference.mkRef(source);
                    EntityReference targetRef = EntityReference.mkRef(target);
                    return Tuple.tuple(sourceRef, targetRef);
                })
                .collect(Collectors.toList());
        return logicalFlowService.findBySourceAndTargetEntityReferences(sourcesAndTargets);
    }


    private LogicalFlow addFlowRoute(Request request, Response response) throws IOException {
        ensureUserHasEditRights(request);

        String username = getUsername(request);

        AddLogicalFlowCommand addCmd = readBody(request, AddLogicalFlowCommand.class);


        LOG.info("User: {}, adding new logical flow: {}", username, addCmd);
        LogicalFlow savedFlow = logicalFlowService.addFlow(addCmd, username);
        return savedFlow;
    }


    private List<LogicalFlow> addFlowsRoute(Request request, Response response) throws IOException {
        ensureUserHasEditRights(request);

        String username = getUsername(request);

        List<AddLogicalFlowCommand> addCmds = Arrays.asList(readBody(request, AddLogicalFlowCommand[].class));
        LOG.info("User: {}, adding new logical flows: {}", username, addCmds);
        List<LogicalFlow> savedFlows = logicalFlowService.addFlows(addCmds, username);
        return savedFlows;
    }


    private int removeFlowRoute(Request request, Response response) {
        ensureUserHasEditRights(request);

        long flowId = getId(request);
        String username = getUsername(request);

        LOG.info("User: {} removing logical flow: {}", username, flowId);

        return logicalFlowService.removeFlow(flowId, username);
    }


    private void ensureUserHasEditRights(Request request) {
        requireRole(userRoleService, request, Role.LOGICAL_DATA_FLOW_EDITOR);
    }


    private void ensureUserHasAdminRights(Request request) {
        requireRole(userRoleService, request, Role.ADMIN);
    }

}
