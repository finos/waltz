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

import org.finos.waltz.service.entity_hierarchy.EntityHierarchyService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.tally.Tally;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForList;
import static org.finos.waltz.web.endpoints.EndpointUtilities.postForDatum;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.model.user.SystemRole.ADMIN;

@Service
public class EntityHierarchyEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(EntityHierarchyEndpoint.class);
    private static final String BASE = mkPath("api", "entity-hierarchy");

    private final EntityHierarchyService entityHierarchyService;
    private final UserRoleService userRoleService;


    @Autowired
    public EntityHierarchyEndpoint(EntityHierarchyService entityHierarchyService, UserRoleService userRoleService) {
        checkNotNull(entityHierarchyService, "entityHierarchyService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.entityHierarchyService = entityHierarchyService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String findTalliesPath = mkPath(BASE, "tallies");
        String findRootTalliesPath = mkPath(BASE, "root-tallies");
        String findRootsPath = mkPath(BASE, "roots", ":kind");
        String buildByKindPath = mkPath(BASE, "build", ":kind");

        ListRoute<Tally<String>> findTalliesRoute = (request, response) -> entityHierarchyService.tallyByKind();
        ListRoute<Tally<String>> findRootTalliesRoute = (request, response) -> entityHierarchyService.getRootTallies();
        ListRoute<EntityReference> findRootsRoute = (request, response) -> entityHierarchyService.getRoots(getKind(request));

        getForList(findTalliesPath, findTalliesRoute);
        getForList(findRootTalliesPath, findRootTalliesRoute);
        getForList(findRootsPath, findRootsRoute);
        postForDatum(buildByKindPath, this::buildByKindRoute);
    }


    private int buildByKindRoute(Request request, Response response) {
        requireRole(userRoleService, request, ADMIN);
        EntityKind kind = getKind(request);
        LOG.info("Building entity hierarchy for kind: {}", kind);
        return entityHierarchyService.buildFor(kind);
    }

}
