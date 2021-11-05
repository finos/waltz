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

import org.finos.waltz.service.relationship_kind.RelationshipKindService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.rel.RelationshipKind;
import org.finos.waltz.model.rel.UpdateRelationshipKindCommand;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class RelationshipKindEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "relationship-kind");

    private final RelationshipKindService relationshipKindService;
    private final UserRoleService userRoleService;

    @Autowired
    public RelationshipKindEndpoint(RelationshipKindService relationshipKindService,
                                    UserRoleService userRoleService) {
        checkNotNull(relationshipKindService, "relationshipKindService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.relationshipKindService = relationshipKindService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {

        String findRelationshipKindsBetweenEntitesPath = WebUtilities.mkPath(BASE_URL, "entities", ":aKind", ":aId", ":bKind", ":bId");
        String removePath = WebUtilities.mkPath(BASE_URL, "id", ":id");
        String createPath = WebUtilities.mkPath(BASE_URL, "create");
        String updatePath = WebUtilities.mkPath(BASE_URL, "id", ":id");

        ListRoute<RelationshipKind> findAllRoute = (req, resp) -> relationshipKindService.findAll();

        ListRoute<RelationshipKind> findRelationshipKindsBetweenEntitesRoute = (req, resp) ->
                relationshipKindService.findRelationshipKindsBetweenEntites(readEntityA(req), readEntityB(req));

        DatumRoute<Boolean> createRoute = (req, resp) -> {
            ensureUserHasAdminRights(req);
            RelationshipKind relationshipKind = WebUtilities.readBody(req, RelationshipKind.class);
            return relationshipKindService.create(relationshipKind);
        };

        DatumRoute<Boolean> updateRoute = (req, resp) -> {
            ensureUserHasAdminRights(req);
            long relKindId = WebUtilities.getId(req);
            UpdateRelationshipKindCommand updateCommand = WebUtilities.readBody(req, UpdateRelationshipKindCommand.class);
            return relationshipKindService.update(relKindId, updateCommand);
        };

        DatumRoute<Boolean> removeRoute = (request, response) -> {
            long relationshipKindId = WebUtilities.getId(request);
            ensureUserHasAdminRights(request);
            return relationshipKindService.remove(relationshipKindId);
        };

        EndpointUtilities.getForList(BASE_URL, findAllRoute);
        EndpointUtilities.getForList(findRelationshipKindsBetweenEntitesPath, findRelationshipKindsBetweenEntitesRoute);
        EndpointUtilities.postForDatum(createPath, createRoute);
        EndpointUtilities.postForDatum(updatePath, updateRoute);
        EndpointUtilities.deleteForDatum(removePath, removeRoute);
    }


    private EntityReference readEntityA(Request req) {
        return WebUtilities.getEntityReference(req, "aKind", "aId");
    }


    private EntityReference readEntityB(Request req) {
        return WebUtilities.getEntityReference(req, "bKind", "bId");
    }

    private void ensureUserHasAdminRights(Request request) {
        WebUtilities.requireRole(userRoleService, request, SystemRole.ADMIN);
    }
}
