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
import com.khartec.waltz.model.actor.Actor;
import com.khartec.waltz.model.actor.ActorChangeCommand;
import com.khartec.waltz.model.actor.ActorCreateCommand;
import com.khartec.waltz.model.command.CommandResponse;
import com.khartec.waltz.model.user.SystemRole;
import com.khartec.waltz.service.actor.ActorService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class ActorEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(ActorEndpoint.class);
    private static final String BASE_URL = WebUtilities.mkPath("api", "actor");

    private final ActorService service;
    private final UserRoleService userRoleService;


    @Autowired
    public ActorEndpoint(ActorService service, UserRoleService userRoleService) {
        checkNotNull(service, "service must not be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.service = service;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {

        String searchPath = mkPath(BASE_URL, "search", ":query");

        ListRoute<EntityReference> searchRoute = (request, response) -> service.search(request.params("query"));


        // read
        getForList(searchPath, searchRoute);
        getForList(BASE_URL, (request, response) -> service.findAll());
        getForDatum(mkPath(BASE_URL, "id", ":id"), this::getByIdRoute );

        // create
        postForDatum(mkPath(BASE_URL, "update"), this::createRoute);

        // save
        putForDatum(mkPath(BASE_URL, "update"), this::updateRoute);

        // delete
        deleteForDatum(mkPath(BASE_URL, ":id"), this::deleteRoute);

    }


    private Actor getByIdRoute(Request request, Response response) {
        long id = getId(request);
        return service.getById(id);
    }


    private Long createRoute(Request request, Response response) throws IOException {
        ensureUserHasAdminRights(request);

        ActorCreateCommand command = readBody(request, ActorCreateCommand.class);
        String username = getUsername(request);
        LOG.info("User: {} creating Involvement Kind: {}", username, command);

        return service.create(command, username);
    }


    private CommandResponse<ActorChangeCommand> updateRoute(Request request, Response response)
            throws IOException {
        ensureUserHasAdminRights(request);

        String username = getUsername(request);
        ActorChangeCommand command = readBody(request, ActorChangeCommand.class);

        LOG.info("User: {} updating Involvement Kind: {}", username, command);
        return service.update(command, username);
    }


    private boolean deleteRoute(Request request, Response response) {
        ensureUserHasAdminRights(request);

        long id = getId(request);
        String username = getUsername(request);

        LOG.info("User: {} removing Involvement Kind: {}", username, id);

        return service.delete(id);
    }


    private void ensureUserHasAdminRights(Request request) {
        requireRole(userRoleService, request, SystemRole.ADMIN);
    }

}
