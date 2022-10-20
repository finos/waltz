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

import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.involvement_kind.InvolvementKindUsageStat;
import org.finos.waltz.service.involvement_kind.InvolvementKindService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.command.CommandResponse;
import org.finos.waltz.model.involvement_kind.InvolvementKind;
import org.finos.waltz.model.involvement_kind.InvolvementKindChangeCommand;
import org.finos.waltz.model.involvement_kind.InvolvementKindCreateCommand;
import org.finos.waltz.model.user.SystemRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;
import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class InvolvementKindEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(InvolvementKindEndpoint.class);
    private static final String BASE_URL = WebUtilities.mkPath("api", "involvement-kind");

    private final InvolvementKindService service;
    private UserRoleService userRoleService;


    @Autowired
    public InvolvementKindEndpoint(InvolvementKindService service, UserRoleService userRoleService) {
        checkNotNull(service, "service must not be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.service = service;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {

        // read
        getForList(BASE_URL, (request, response) -> service.findAll());

        getForList(mkPath(BASE_URL, "key-involvement-kinds", ":kind"), this::findKeyInvolvementKindByEntityKind);

        getForList(mkPath(BASE_URL, "usage-stats"), this::loadUsageStats);
        getForDatum(mkPath(BASE_URL, "usage-stats", "kind", ":id"), this::loadUsageStatsForKindRoute);

        getForDatum(mkPath(BASE_URL, "id", ":id"), this::getByIdRoute);
        getForDatum(mkPath(BASE_URL, "external-id", ":externalId"), this::getByExternalIdRoute);

        // create
        postForDatum(mkPath(BASE_URL, "update"), this::createInvolvementKindRoute);

        // save
        putForDatum(mkPath(BASE_URL, "update"), this::updateInvolvementKindRoute);

        // delete
        deleteForDatum(mkPath(BASE_URL, ":id"), this::deleteInvolvementKindRoute);

    }


    private InvolvementKind getByIdRoute(Request request, Response response) {
        long id = getId(request);
        return service.getById(id);
    }

    private InvolvementKind getByExternalIdRoute(Request request, Response response) {
        String externalId = request.params("externalId");
        return service.getByExternalId(externalId);
    }


    private List<InvolvementKind> findKeyInvolvementKindByEntityKind(Request request, Response response) {
        EntityKind entityKind = getKind(request);
        return service.findKeyInvolvementKindsByEntityKind(entityKind);
    }


    private Long createInvolvementKindRoute(Request request, Response response) throws IOException {
        ensureUserHasAdminRights(request);

        InvolvementKindCreateCommand command = readBody(request, InvolvementKindCreateCommand.class);
        String username = getUsername(request);
        LOG.info("User: {} creating Involvement Kind: {}", username, command);

        return service.create(command, username);
    }


    private CommandResponse<InvolvementKindChangeCommand> updateInvolvementKindRoute(Request request, Response response)
            throws IOException {
        ensureUserHasAdminRights(request);

        String username = getUsername(request);
        InvolvementKindChangeCommand command = readBody(request, InvolvementKindChangeCommand.class);

        LOG.info("User: {} updating Involvement Kind: {}", username, command);
        return service.update(command, username);
    }


    private boolean deleteInvolvementKindRoute(Request request, Response response) {
        ensureUserHasAdminRights(request);

        long id = getId(request);
        String username = getUsername(request);

        LOG.info("User: {} removing Involvement Kind: {}", username, id);

        return service.delete(id);
    }


    private Set<InvolvementKindUsageStat> loadUsageStats(Request request, Response response) {
        return service.loadUsageStats();
    }


    private InvolvementKindUsageStat loadUsageStatsForKindRoute(Request request, Response response) {
        return service.loadUsageStatsForKind(getId(request));
    }


    private void ensureUserHasAdminRights(Request request) {
        requireRole(userRoleService, request, SystemRole.ADMIN);
    }

}
