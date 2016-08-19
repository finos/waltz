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
import com.khartec.waltz.model.dataflow.DataFlowStatistics;
import com.khartec.waltz.model.dataflow.ImmutableDataFlow;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.data_flow.DataFlowService;
import com.khartec.waltz.service.usage_info.DataTypeUsageService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.action.UpdateDataFlowsAction;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class DataFlowsEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "data-flows");

    private final DataFlowService dataFlowService;
    private final DataTypeUsageService dataTypeUsageService;
    private final ChangeLogService changeLogService;
    private final UserRoleService userRoleService;


    @Autowired
    public DataFlowsEndpoint(DataFlowService dataFlowService,
                             DataTypeUsageService dataTypeUsageService,
                             ChangeLogService changeLogService,
                             UserRoleService userRoleService) {
        checkNotNull(dataFlowService, "dataFlowService must not be null");
        checkNotNull(dataTypeUsageService, "dataTypeUsageService cannot be null");
        checkNotNull(changeLogService, "changeLogService must not be null");
        checkNotNull(userRoleService, "userRoleService must not be null");

        this.dataFlowService = dataFlowService;
        this.dataTypeUsageService = dataTypeUsageService;
        this.changeLogService = changeLogService;
        this.userRoleService = userRoleService;

    }


    @Override
    public void register() {

        String findByEntityPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String findByAppIdSelectorPath = mkPath(BASE_URL, "apps");
        String findByDataTypeIdSelectorPath = mkPath(BASE_URL, "data-type");
        String findStatsPath = mkPath(BASE_URL, "stats");
        String findStatsForDataTypePath = mkPath(BASE_URL, "stats", "data-type");
        String updateFlowsPath = BASE_URL;
        String tallyByDataTypePath = mkPath(BASE_URL, "count-by", "data-type");


        DatumRoute<Boolean> updateFlowsRoute = (request, response) -> {
            requireRole(userRoleService, request, Role.LOGICAL_DATA_FLOW_EDITOR);

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

            dataTypeUsageService.recalculateForApplications(dataFlowUpdate.target(), dataFlowUpdate.source());
            return true;
        };


        ListRoute<DataFlow> getByEntityRef = (request, response) -> {
            EntityReference ref = getEntityReference(request);
            return dataFlowService.findByEntityReference(ref);
        };

        ListRoute<DataFlow> findByAppIdSelectorRoute = (request, response)
                -> dataFlowService.findByAppIdSelector(readIdSelectionOptionsFromBody(request));

        ListRoute<DataFlow> findByDataTypeIdSelectorRoute = (request, response)
                -> dataFlowService.findByDataTypeIdSelector(readIdSelectionOptionsFromBody(request));

        DatumRoute<DataFlowStatistics> findStatsRoute = (request, response)
                -> dataFlowService.calculateStats(readIdSelectionOptionsFromBody(request));

        DatumRoute<DataFlowStatistics> findStatsForDataTypeRoute = (request, response)
                -> dataFlowService.calculateStatsForDataType(readIdSelectionOptionsFromBody(request));


        ListRoute<Tally<String>> tallyByDataTypeRoute = (request, response)
                -> dataFlowService.tallyByDataType();


        getForList(findByEntityPath, getByEntityRef);
        postForList(findByAppIdSelectorPath, findByAppIdSelectorRoute);
        postForList(findByDataTypeIdSelectorPath, findByDataTypeIdSelectorRoute);
        postForDatum(updateFlowsPath, updateFlowsRoute);
        postForDatum(findStatsPath, findStatsRoute);
        postForDatum(findStatsForDataTypePath, findStatsForDataTypeRoute);
        getForList(tallyByDataTypePath, tallyByDataTypeRoute);
    }


}
