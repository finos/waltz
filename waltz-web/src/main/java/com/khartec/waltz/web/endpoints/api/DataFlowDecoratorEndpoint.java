/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import com.khartec.waltz.model.data_flow_decorator.DataFlowDecorator;
import com.khartec.waltz.model.data_flow_decorator.DecoratorRatingSummary;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.data_flow_decorator.DataFlowDecoratorService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.action.UpdateDataFlowDecoratorsAction;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.CollectionUtilities.notEmpty;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;

/**
 * Created by dwatkins on 30/08/2016.
 */
@Service
public class DataFlowDecoratorEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(DataFlowDecoratorEndpoint.class);
    private static final String BASE_URL = mkPath("api", "data-flow-decorator");


    private final DataFlowDecoratorService dataFlowDecoratorService;
    private final UserRoleService userRoleService;


    @Autowired
    public DataFlowDecoratorEndpoint(DataFlowDecoratorService dataFlowDecoratorService,
                                     UserRoleService userRoleService) {
        checkNotNull(dataFlowDecoratorService, "dataFlowDecoratorService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");
        this.dataFlowDecoratorService = dataFlowDecoratorService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String findByIdSelectorAndKindPath = mkPath(BASE_URL, "kind", ":kind");
        String findByIdSelectorPath = mkPath(BASE_URL, "selector");
        String updateDecoratorsPath = mkPath(BASE_URL, ":flowId");
        String summarizeForSelectorPath = mkPath(BASE_URL, "summarize");

        ListRoute<DataFlowDecorator> findByIdSelectorAndKindRoute =
                (request, response) -> dataFlowDecoratorService
                        .findByIdSelectorAndKind(
                                readIdSelectionOptionsFromBody(request),
                                getKind(request));

        ListRoute<DataFlowDecorator> findByIdSelectorRoute =
                (request, response) -> dataFlowDecoratorService
                        .findBySelector(
                                readIdSelectionOptionsFromBody(request));

        ListRoute<DecoratorRatingSummary> summarizeForSelectorRoute =
                (request, response) -> dataFlowDecoratorService.summarizeForSelector(
                        readIdSelectionOptionsFromBody(request));

        postForList(
                findByIdSelectorAndKindPath,
                findByIdSelectorAndKindRoute);

        postForList(
                findByIdSelectorPath,
                findByIdSelectorRoute);

        postForList(
                summarizeForSelectorPath,
                summarizeForSelectorRoute);

        postForList(
                updateDecoratorsPath,
                this::updateDecoratorsRoute);
    }


    private Collection<DataFlowDecorator> updateDecoratorsRoute(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, Role.LOGICAL_DATA_FLOW_EDITOR);

        String user = getUsername(request);
        UpdateDataFlowDecoratorsAction action = readBody(request, UpdateDataFlowDecoratorsAction.class);

        if (notEmpty(action.removedDecorators())) {
            LOG.info("User: {}, deleting decorators: {} for flow: {}", user, action.removedDecorators(), action.flowId());
            dataFlowDecoratorService.deleteDecorators(action.flowId(), action.removedDecorators(), user);
        }

        if (notEmpty(action.addedDecorators())) {
            LOG.info("User: {}, adding decorators: {} for flow: {}", user, action.addedDecorators(), action.flowId());
            dataFlowDecoratorService.addDecorators(action.flowId(), action.addedDecorators(), user);
        }

        return dataFlowDecoratorService.findByFlowIds(newArrayList(action.flowId()));
    }


}
