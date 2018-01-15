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

import com.khartec.waltz.model.data_flow_decorator.LogicalFlowDecorator;
import com.khartec.waltz.model.data_flow_decorator.DecoratorRatingSummary;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.data_flow_decorator.LogicalFlowDecoratorService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.model.data_flow_decorator.UpdateDataFlowDecoratorsAction;
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
import static com.khartec.waltz.common.CollectionUtilities.notEmpty;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;


@Service
public class LogicalFlowDecoratorEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(LogicalFlowDecoratorEndpoint.class);
    private static final String BASE_URL = mkPath("api", "logical-flow-decorator");


    private final LogicalFlowDecoratorService logicalFlowDecoratorService;
    private final UserRoleService userRoleService;


    @Autowired
    public LogicalFlowDecoratorEndpoint(LogicalFlowDecoratorService logicalFlowDecoratorService,
                                        UserRoleService userRoleService) {
        checkNotNull(logicalFlowDecoratorService, "logicalFlowDecoratorService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");
        this.logicalFlowDecoratorService = logicalFlowDecoratorService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String findByIdSelectorAndKindPath = mkPath(BASE_URL, "selector", "kind", ":kind");
        String findByIdSelectorPath = mkPath(BASE_URL, "selector");
        String findByFlowIdsAndKindPath = mkPath(BASE_URL, "flow-ids", "kind", ":kind");
        String updateDecoratorsPath = mkPath(BASE_URL, ":flowId");
        String updateDecoratorsBatchPath = mkPath(BASE_URL, "batch");
        String summarizeInboundPath = mkPath(BASE_URL, "summarize-inbound");
        String summarizeOutboundPath = mkPath(BASE_URL, "summarize-outbound");

        ListRoute<LogicalFlowDecorator> findByIdSelectorAndKindRoute =
                (request, response) -> logicalFlowDecoratorService
                        .findByIdSelectorAndKind(
                                readIdSelectionOptionsFromBody(request),
                                getKind(request));

        ListRoute<LogicalFlowDecorator> findByIdSelectorRoute =
                (request, response) -> logicalFlowDecoratorService
                        .findBySelector(
                                readIdSelectionOptionsFromBody(request));

        ListRoute<LogicalFlowDecorator> findByFlowIdsAndKindRoute =
                (request, response) -> logicalFlowDecoratorService
                        .findByFlowIdsAndKind(
                                readIdsFromBody(request), getKind(request));

        ListRoute<DecoratorRatingSummary> summarizeInboundForSelectorRoute =
                (request, response) -> logicalFlowDecoratorService.summarizeInboundForSelector(
                        readIdSelectionOptionsFromBody(request));

        ListRoute<DecoratorRatingSummary> summarizeOutboundForSelectorRoute =
                (request, response) -> logicalFlowDecoratorService.summarizeOutboundForSelector(
                        readIdSelectionOptionsFromBody(request));

        ListRoute<DecoratorRatingSummary> summarizeForAllRoute =
                (request, response) -> logicalFlowDecoratorService.summarizeForAll();

        postForList(
                findByIdSelectorAndKindPath,
                findByIdSelectorAndKindRoute);

        postForList(
                findByFlowIdsAndKindPath,
                findByFlowIdsAndKindRoute);

        postForList(
                findByIdSelectorPath,
                findByIdSelectorRoute);

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
                updateDecoratorsPath,
                this::updateDecoratorsRoute);
    }


    private Collection<LogicalFlowDecorator> updateDecoratorsRoute(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, Role.LOGICAL_DATA_FLOW_EDITOR);

        String user = getUsername(request);
        UpdateDataFlowDecoratorsAction action = readBody(request, UpdateDataFlowDecoratorsAction.class);

        if (notEmpty(action.removedDecorators())) {
            LOG.info("User: {}, deleting decorators: {} for flow: {}", user, action.removedDecorators(), action.flowId());
            logicalFlowDecoratorService.deleteDecorators(action.flowId(), action.removedDecorators(), user);
        }

        if (notEmpty(action.addedDecorators())) {
            LOG.info("User: {}, adding decorators: {} for flow: {}", user, action.addedDecorators(), action.flowId());
            logicalFlowDecoratorService.addDecorators(action.flowId(), action.addedDecorators(), user);
        }

        return logicalFlowDecoratorService.findByFlowIds(newArrayList(action.flowId()));
    }


    private Collection<LogicalFlowDecorator> updateDecoratorsBatchRoute(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, Role.LOGICAL_DATA_FLOW_EDITOR);

        String user = getUsername(request);
        List<UpdateDataFlowDecoratorsAction> actions = Arrays.asList(readBody(request, UpdateDataFlowDecoratorsAction[].class));

        logicalFlowDecoratorService.addDecoratorsBatch(actions, user);
        return logicalFlowDecoratorService.findByFlowIds(map(actions, a -> a.flowId()));
    }

}
