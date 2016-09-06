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

import com.khartec.waltz.model.dataflow.DataFlow;
import com.khartec.waltz.model.dataflow.DataFlowStatistics;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.data_flow.DataFlowService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class DataFlowsEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(DataFlowsEndpoint.class);
    private static final String BASE_URL = mkPath("api", "data-flows");

    private final DataFlowService dataFlowService;
    private final UserRoleService userRoleService;


    @Autowired
    public DataFlowsEndpoint(DataFlowService dataFlowService,
                             UserRoleService userRoleService) {
        checkNotNull(dataFlowService, "dataFlowService must not be null");
        checkNotNull(userRoleService, "userRoleService must not be null");

        this.dataFlowService = dataFlowService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String findByEntityPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String findByAppIdSelectorPath = mkPath(BASE_URL, "apps");
        String findStatsPath = mkPath(BASE_URL, "stats");
        String tallyByDataTypePath = mkPath(BASE_URL, "count-by", "data-type");
        String findByDataTypeIdSelectorPath = mkPath(BASE_URL, "data-type");

        String removeFlowPath = mkPath(BASE_URL, ":id");
        String addFlowPath = mkPath(BASE_URL);

        ListRoute<DataFlow> getByEntityRef = (request, response)
                -> dataFlowService.findByEntityReference(getEntityReference(request));

        ListRoute<DataFlow> findByAppIdSelectorRoute = (request, response)
                -> dataFlowService.findByAppIdSelector(readIdSelectionOptionsFromBody(request));

        DatumRoute<DataFlowStatistics> findStatsRoute = (request, response)
                -> dataFlowService.calculateStats(readIdSelectionOptionsFromBody(request));

        ListRoute<Tally<String>> tallyByDataTypeRoute = (request, response)
                -> dataFlowService.tallyByDataType();

        ListRoute<DataFlow> findByDataTypeIdSelectorRoute = (request, response)
                -> dataFlowService.findByDataTypeIdSelector(readIdSelectionOptionsFromBody(request));

        getForList(findByEntityPath, getByEntityRef);
        postForList(findByAppIdSelectorPath, findByAppIdSelectorRoute);
        postForList(findByDataTypeIdSelectorPath, findByDataTypeIdSelectorRoute);
        
        postForDatum(findStatsPath, findStatsRoute);
        getForList(tallyByDataTypePath, tallyByDataTypeRoute);

        deleteForDatum(removeFlowPath, this::removeFlowRoute);
        postForDatum(addFlowPath, this::addFlowRoute);
    }

    private DataFlow addFlowRoute(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, Role.LOGICAL_DATA_FLOW_EDITOR);

        DataFlow dataFlow = readBody(request, DataFlow.class);
        String username = getUsername(request);

        if (dataFlow.id().isPresent()) {
            LOG.warn("User: {}, ignoring attempt to add duplicate flow: {}", username, dataFlow);
            return dataFlow;
        }

        LOG.info("User: {}, adding new flow: {}", username, dataFlow);
        DataFlow savedFlow = dataFlowService.addFlow(dataFlow);
        return savedFlow;
    }


    private int removeFlowRoute(Request request, Response response) {
        requireRole(userRoleService, request, Role.LOGICAL_DATA_FLOW_EDITOR);

        long flowId = getId(request);
        String username = getUsername(request);

        LOG.info("User: {} removing logical flow: {}", username, flowId);

        return dataFlowService.removeFlows(newArrayList(flowId));
    }

}
