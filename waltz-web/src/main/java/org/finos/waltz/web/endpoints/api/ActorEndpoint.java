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

import org.finos.waltz.service.actor.ActorService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.actor.Actor;
import org.finos.waltz.model.actor.ActorChangeCommand;
import org.finos.waltz.model.actor.ActorCreateCommand;
import org.finos.waltz.model.command.CommandResponse;
import org.finos.waltz.model.user.SystemRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;
import static org.finos.waltz.common.Checks.checkNotNull;

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
        requireRole(userRoleService, request, SystemRole.ACTOR_ADMIN);
    }

}
