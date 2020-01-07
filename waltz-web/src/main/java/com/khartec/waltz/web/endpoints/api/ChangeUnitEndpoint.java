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


import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.change_unit.ChangeUnit;
import com.khartec.waltz.model.change_unit.UpdateExecutionStatusCommand;
import com.khartec.waltz.model.command.CommandResponse;
import com.khartec.waltz.model.user.SystemRole;
import com.khartec.waltz.service.change_unit.ChangeUnitService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;
import static java.lang.Long.parseLong;


@Service
public class ChangeUnitEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeUnitEndpoint.class);
    private static final String BASE_URL = mkPath("api", "change-unit");

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
        String getByIdPath = mkPath(BASE_URL, "id", ":id");
        String findByChangeSetIdPath = mkPath(BASE_URL, "change-set", ":id");
        String findBySubjectRefPath = mkPath(BASE_URL, "subject", ":kind", ":id");
        String findBySelectorPath = mkPath(BASE_URL, "selector");
        String updateExecutionStatusPath = mkPath(BASE_URL, "update", "execution-status");

        DatumRoute<ChangeUnit> getByIdRoute = (request, response) -> {
            String id = request.params("id");
            return service.getById(parseLong(id));
        };

        ListRoute<ChangeUnit> findBySubjectRefRoute = (request, response) -> {
            EntityReference entityReference = getEntityReference(request);
            return service.findBySubjectRef(entityReference);
        };

        ListRoute<ChangeUnit> findByChangeSetIdRoute = (request, response) -> {
            String id = request.params("id");
            return service.findByChangeSetId(parseLong(id));
        };

        ListRoute<ChangeUnit> findBySelectorRoute = ((request, response)
                -> service.findBySelector(readIdSelectionOptionsFromBody(request)));

        DatumRoute<CommandResponse<UpdateExecutionStatusCommand>> updateExecutionStatusRoute = (request, response) -> {
            ensureUserEditRights(request);
            String username = getUsername(request);
            UpdateExecutionStatusCommand command = readBody(request, UpdateExecutionStatusCommand.class);

            LOG.info("User: {} updating Change Unit Execution Status: {}", username, command);
            return service.updateExecutionStatus(command, username);
        };


        getForDatum(getByIdPath, getByIdRoute);
        getForList(findBySubjectRefPath, findBySubjectRefRoute);
        getForList(findByChangeSetIdPath, findByChangeSetIdRoute);
        postForList(findBySelectorPath, findBySelectorRoute);
        postForDatum(updateExecutionStatusPath, updateExecutionStatusRoute);
    }


    private void ensureUserEditRights(Request request) {
        requireRole(userRoleService, request, SystemRole.CHANGE_SET_EDITOR);
    }

}
