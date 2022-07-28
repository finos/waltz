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

import org.finos.waltz.service.data_flow_decorator.LogicalFlowDecoratorService;
import org.finos.waltz.service.data_type.DataTypeDecoratorService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.data_flow_decorator.DecoratorRatingSummary;
import org.finos.waltz.model.data_flow_decorator.LogicalFlowDecoratorStat;
import org.finos.waltz.model.data_flow_decorator.UpdateDataFlowDecoratorsAction;
import org.finos.waltz.model.datatype.DataTypeDecorator;
import org.finos.waltz.model.user.SystemRole;
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

import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForList;
import static org.finos.waltz.web.endpoints.EndpointUtilities.postForList;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.CollectionUtilities.map;
import static org.finos.waltz.model.EntityKind.LOGICAL_DATA_FLOW;


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
        requireRole(userRoleService, request, SystemRole.BULK_FLOW_EDITOR);

        String user = getUsername(request);
        List<UpdateDataFlowDecoratorsAction> actions = Arrays.asList(readBody(request, UpdateDataFlowDecoratorsAction[].class));

        logicalFlowDecoratorService.addDecoratorsBatch(actions, user);
        return dataTypeDecoratorService.findByFlowIds(map(actions, a -> a.flowId()), LOGICAL_DATA_FLOW);
    }

}
