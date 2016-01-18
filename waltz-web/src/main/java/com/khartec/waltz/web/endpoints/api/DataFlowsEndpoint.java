/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.dataflow.DataFlow;
import com.khartec.waltz.model.dataflow.ImmutableDataFlow;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.data_flow.DataFlowService;
import com.khartec.waltz.service.user.UserService;
import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.action.UpdateDataFlowsAction;
import com.khartec.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Route;

import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static spark.Spark.post;


@Service
public class DataFlowsEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "data-flows");

    private final DataFlowService dataFlowService;
    private final ChangeLogService changeLogService;
    private final UserService userService;


    @Autowired
    public DataFlowsEndpoint(DataFlowService dataFlowService, ChangeLogService changeLogService, UserService userService) {
        checkNotNull(dataFlowService, "dataFlowService must not be null");
        checkNotNull(changeLogService, "changeLogService must not be null");
        checkNotNull(userService, "userService must not be null");

        this.dataFlowService = dataFlowService;
        this.changeLogService = changeLogService;
        this.userService = userService;

    }


    @Override
    public void register() {

        ListRoute<DataFlow> getByEntityRef = (request, response) -> {
            EntityReference ref = getEntityReference(request);
            return dataFlowService.findByEntityReference(ref);
        };

        ListRoute<DataFlow> getByOrgUnit = (request, response) -> {
            long orgUnitId = getLong(request, "orgUnitId");
            return dataFlowService.findByOrganisationalUnitId(orgUnitId);
        };

        ListRoute<DataFlow> getByOrgUnitTree = (request, response) -> {
            long orgUnitId = getLong(request, "orgUnitId");
            return dataFlowService.findByOrganisationalUnitTree(orgUnitId);
        };

        ListRoute<Application> findApplicationsByEntityRef = (request, response) -> {
            EntityReference ref = getEntityReference(request);
            return dataFlowService.findApplicationsByEntityReference(ref);
        };

        ListRoute<DataFlow> findByCapability = (request, response) ->
                dataFlowService.findByCapability(getId(request));

        Route create = (request, response) -> {
            response.type(TYPE_JSON);

            requireRole(userService, request, Role.APP_EDITOR);

            UpdateDataFlowsAction dataFlowUpdate = WebUtilities.readBody(request, UpdateDataFlowsAction.class);
            List<DataFlow> addedFlows = dataFlowUpdate
                    .addedTypes()
                    .stream()
                    .map(a -> ImmutableDataFlow.builder()
                            .source(dataFlowUpdate.source())
                            .target(dataFlowUpdate.target())
                            .dataType(a)
                            .build())
                    .collect(Collectors.toList());


            List<DataFlow> removedFlows = dataFlowUpdate
                    .removedTypes()
                    .stream()
                    .map(a -> ImmutableDataFlow.builder()
                            .source(dataFlowUpdate.source())
                            .target(dataFlowUpdate.target())
                            .dataType(a)
                            .build())
                    .collect(Collectors.toList());


            dataFlowService
                    .addFlows(addedFlows);

            dataFlowService
                    .removeFlows(removedFlows);

            String message = String.format(
                    "Flows updated between %s and %s, added: %s types and removed: %s types",
                    dataFlowUpdate.source().name().orElse("?"),
                    dataFlowUpdate.target().name().orElse("?"),
                    dataFlowUpdate.addedTypes(),
                    dataFlowUpdate.removedTypes());

            changeLogService.write(ImmutableChangeLog.builder()
                    .userId(getUser(request).userName())
                    .parentReference(dataFlowUpdate.source())
                    .severity(Severity.INFORMATION)
                    .message(message)
                    .build());


            changeLogService.write(ImmutableChangeLog.builder()
                    .userId(getUser(request).userName())
                    .parentReference(dataFlowUpdate.target())
                    .severity(Severity.INFORMATION)
                    .message(message)
                    .build());


            return null;
        };


        EndpointUtilities.getForList(mkPath(BASE_URL, "entity", ":kind", ":id"), getByEntityRef);
        EndpointUtilities.getForList(mkPath(BASE_URL, "entity", ":kind", ":id", "applications"), findApplicationsByEntityRef);
        EndpointUtilities.getForList(mkPath(BASE_URL, "org-unit", ":orgUnitId"), getByOrgUnit);
        EndpointUtilities.getForList(mkPath(BASE_URL, "org-unit-tree", ":orgUnitId"), getByOrgUnitTree);

        EndpointUtilities.getForList(mkPath(BASE_URL, "capability", ":id"), findByCapability);
        post(BASE_URL, create, transformer);
    }

}
