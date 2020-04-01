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

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.data_flow_decorator.DecoratorRatingSummary;
import com.khartec.waltz.model.data_flow_decorator.LogicalFlowDecoratorStat;
import com.khartec.waltz.model.data_flow_decorator.UpdateDataFlowDecoratorsAction;
import com.khartec.waltz.model.datatype.DataTypeDecorator;
import com.khartec.waltz.model.user.SystemRole;
import com.khartec.waltz.service.data_flow_decorator.LogicalFlowDecoratorService;
import com.khartec.waltz.service.data_type.DataTypeDecoratorService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.CollectionUtilities.map;
import static com.khartec.waltz.model.EntityKind.LOGICAL_DATA_FLOW;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;


@Service
public class LogicalFlowDecoratorStatsEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(LogicalFlowDecoratorStatsEndpoint.class);
    private static final String BASE_URL = mkPath("api", "logical-flow-decorator");


    private final LogicalFlowDecoratorService logicalFlowDecoratorService;
    private final DataTypeDecoratorService dataTypeDecoratorService;
    private final UserRoleService userRoleService;


    @Autowired
    public LogicalFlowDecoratorStatsEndpoint(LogicalFlowDecoratorService logicalFlowDecoratorService,
                                             DataTypeDecoratorService dataTypeDecoratorService, UserRoleService userRoleService) {
        checkNotNull(logicalFlowDecoratorService, "logicalFlowDecoratorService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");
        this.logicalFlowDecoratorService = logicalFlowDecoratorService;
        this.userRoleService = userRoleService;
        this.dataTypeDecoratorService = dataTypeDecoratorService;
    }


    @Override
    public void register() {
        String updateDecoratorsBatchPath = mkPath(BASE_URL, "update", "batch");
        String summarizeInboundPath = mkPath(BASE_URL, "summarize-inbound");
        String summarizeOutboundPath = mkPath(BASE_URL, "summarize-outbound");
        String findDataTypeStatsForEntityPath = mkPath(BASE_URL, "datatype-stats");

        ListRoute<DecoratorRatingSummary> summarizeInboundForSelectorRoute =
                (request, response) -> logicalFlowDecoratorService.summarizeInboundForSelector(
                        readIdSelectionOptionsFromBody(request));

        ListRoute<DecoratorRatingSummary> summarizeOutboundForSelectorRoute =
                (request, response) -> logicalFlowDecoratorService.summarizeOutboundForSelector(
                        readIdSelectionOptionsFromBody(request));

        ListRoute<DecoratorRatingSummary> summarizeForAllRoute =
                (request, response) -> logicalFlowDecoratorService.summarizeForAll();

        ListRoute<LogicalFlowDecoratorStat> findDatatypeStatsForEntityRoute =
                (request, response) -> {
                    IdSelectionOptions selectionOptions = readIdSelectionOptionsFromBody(request);
                    return logicalFlowDecoratorService.findFlowsByDatatypeForEntity(selectionOptions);
                };

        postForList(
                summarizeInboundPath,
                summarizeInboundForSelectorRoute);

        postForList(
                summarizeOutboundPath,
                summarizeOutboundForSelectorRoute);

        getForList(
                summarizeInboundPath,
                summarizeForAllRoute);

        postForList(
                updateDecoratorsBatchPath,
                this::updateDecoratorsBatchRoute);

        postForList(
                findDataTypeStatsForEntityPath,
                findDatatypeStatsForEntityRoute);
    }


    private Collection<DataTypeDecorator> updateDecoratorsBatchRoute(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, SystemRole.LOGICAL_DATA_FLOW_EDITOR);

        String user = getUsername(request);
        List<UpdateDataFlowDecoratorsAction> actions = Arrays.asList(readBody(request, UpdateDataFlowDecoratorsAction[].class));

        logicalFlowDecoratorService.addDecoratorsBatch(actions, user);
        return dataTypeDecoratorService.findByFlowIds(map(actions, a -> a.flowId()), LOGICAL_DATA_FLOW);
    }

}
