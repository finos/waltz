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
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.dataflow.DataFlow;
import com.khartec.waltz.model.dataflow.ImmutableDataFlow;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.data_flow.DataFlowService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.action.UpdateDataFlowsAction;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Route;

import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;
import static spark.Spark.post;


@Service
public class DataFlowsEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "data-flows");

    private final DataFlowService dataFlowService;
    private final ChangeLogService changeLogService;
    private final UserRoleService userRoleService;


    @Autowired
    public DataFlowsEndpoint(DataFlowService dataFlowService,
                             ChangeLogService changeLogService,
                             UserRoleService userRoleService) {
        checkNotNull(dataFlowService, "dataFlowService must not be null");
        checkNotNull(changeLogService, "changeLogService must not be null");
        checkNotNull(userRoleService, "userRoleService must not be null");

        this.dataFlowService = dataFlowService;
        this.changeLogService = changeLogService;
        this.userRoleService = userRoleService;

    }


    @Override
    public void register() {

        ListRoute<DataFlow> getByEntityRef = (request, response) -> {
            EntityReference ref = getEntityReference(request);
            return dataFlowService.findByEntityReference(ref);
        };

        ListRoute<DataFlow> findByAppIds = (request, response) -> {
            List<Long> appIds = (List<Long>) readBody(request, List.class);
            return dataFlowService.findByAppIds(appIds);
        };

        Route create = (request, response) -> {
            response.type(TYPE_JSON);

            requireRole(userRoleService, request, Role.APP_EDITOR);

            UpdateDataFlowsAction dataFlowUpdate = readBody(request, UpdateDataFlowsAction.class);
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
                    .userId(getUsername(request))
                    .parentReference(dataFlowUpdate.source())
                    .severity(Severity.INFORMATION)
                    .message(message)
                    .build());


            changeLogService.write(ImmutableChangeLog.builder()
                    .userId(getUsername(request))
                    .parentReference(dataFlowUpdate.target())
                    .severity(Severity.INFORMATION)
                    .message(message)
                    .build());


            return null;
        };

        getForList(mkPath(BASE_URL, "entity", ":kind", ":id"), getByEntityRef);

        postForList(mkPath(BASE_URL, "apps"), findByAppIds);

        post(BASE_URL, create, transformer);
    }

}
