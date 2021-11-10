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


import org.finos.waltz.service.change_unit.ChangeUnitService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.change_unit.ChangeUnit;
import org.finos.waltz.model.change_unit.UpdateExecutionStatusCommand;
import org.finos.waltz.model.command.CommandResponse;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static java.lang.Long.parseLong;
import static org.finos.waltz.common.Checks.checkNotNull;


@Service
public class ChangeUnitEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeUnitEndpoint.class);
    private static final String BASE_URL = WebUtilities.mkPath("api", "change-unit");

    private final ChangeUnitService service;
    private final UserRoleService userRoleService;


    @Autowired
    public ChangeUnitEndpoint(ChangeUnitService service,
                              UserRoleService userRoleService) {
        checkNotNull(service, "service cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.service = service;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String getByIdPath = WebUtilities.mkPath(BASE_URL, "id", ":id");
        String findByChangeSetIdPath = WebUtilities.mkPath(BASE_URL, "change-set", ":id");
        String findBySubjectRefPath = WebUtilities.mkPath(BASE_URL, "subject", ":kind", ":id");
        String findBySelectorPath = WebUtilities.mkPath(BASE_URL, "selector");
        String updateExecutionStatusPath = WebUtilities.mkPath(BASE_URL, "update", "execution-status");

        DatumRoute<ChangeUnit> getByIdRoute = (request, response) -> {
            String id = request.params("id");
            return service.getById(parseLong(id));
        };

        ListRoute<ChangeUnit> findBySubjectRefRoute = (request, response) -> {
            EntityReference entityReference = WebUtilities.getEntityReference(request);
            return service.findBySubjectRef(entityReference);
        };

        ListRoute<ChangeUnit> findByChangeSetIdRoute = (request, response) -> {
            String id = request.params("id");
            return service.findByChangeSetId(parseLong(id));
        };

        ListRoute<ChangeUnit> findBySelectorRoute = ((request, response)
                -> service.findBySelector(WebUtilities.readIdSelectionOptionsFromBody(request)));

        DatumRoute<CommandResponse<UpdateExecutionStatusCommand>> updateExecutionStatusRoute = (request, response) -> {
            ensureUserEditRights(request);
            String username = WebUtilities.getUsername(request);
            UpdateExecutionStatusCommand command = WebUtilities.readBody(request, UpdateExecutionStatusCommand.class);

            LOG.info("User: {} updating Change Unit Execution Status: {}", username, command);
            return service.updateExecutionStatus(command, username);
        };


        EndpointUtilities.getForDatum(getByIdPath, getByIdRoute);
        EndpointUtilities.getForList(findBySubjectRefPath, findBySubjectRefRoute);
        EndpointUtilities.getForList(findByChangeSetIdPath, findByChangeSetIdRoute);
        EndpointUtilities.postForList(findBySelectorPath, findBySelectorRoute);
        EndpointUtilities.postForDatum(updateExecutionStatusPath, updateExecutionStatusRoute);
    }


    private void ensureUserEditRights(Request request) {
        WebUtilities.requireRole(userRoleService, request, SystemRole.CHANGE_SET_EDITOR);
    }

}
