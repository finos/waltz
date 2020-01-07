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

import com.khartec.waltz.common.EnumUtilities;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.roadmap.RoadmapCreateCommand;
import com.khartec.waltz.model.user.SystemRole;
import com.khartec.waltz.service.roadmap.RoadmapService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class RoadmapEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "roadmap");
    private final RoadmapService roadmapService;
    private final UserRoleService userRoleService;


    @Autowired
    public RoadmapEndpoint(RoadmapService roadmapService, UserRoleService userRoleService) {
        checkNotNull(roadmapService, "roadmapService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.roadmapService = roadmapService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        registerFindRoadmapsBySelector(mkPath(BASE_URL, "by-selector"));
        registerGetRoadmapById(mkPath(BASE_URL, "id", ":id"));
        registerUpdateName(mkPath(BASE_URL, "id", ":id", "name"));
        registerUpdateDescription(mkPath(BASE_URL, "id", ":id", "description"));
        registerUpdateLifecycleStatus(mkPath(BASE_URL, "id", ":id", "lifecycleStatus"));
        registerAddScenario(mkPath(BASE_URL, "id", ":id", "add-scenario"));
        registerFindAllRoadmapsAndScenarios(BASE_URL);
        registerFindRoadmapsAndScenariosByRatedEntity(mkPath(BASE_URL, "by-rated-entity", ":kind", ":id"));
        registerFindRoadmapsAndScenariosByFormalRelationship(mkPath(BASE_URL, "by-formal-relationship", ":kind", ":id"));
        registerCreateRoadmap(mkPath(BASE_URL));
    }


    private void registerCreateRoadmap(String path) {
        postForDatum(path, ((request, response) -> {
            ensureUserHasAdminRights(request);
            RoadmapCreateCommand createCommand = readBody(request, RoadmapCreateCommand.class);
            return roadmapService.createRoadmap(createCommand, getUsername(request));
        }));
    }


    private void registerFindAllRoadmapsAndScenarios(String path) {
        getForList(path, (request, response) ->
                roadmapService.findAllRoadmapsAndScenarios());
    }


    private void registerFindRoadmapsAndScenariosByFormalRelationship(String path) {
        getForList(path, (request, response) ->
                roadmapService.findRoadmapsAndScenariosByFormalRelationship(
                        getEntityReference(request)));
    }


    private void registerFindRoadmapsAndScenariosByRatedEntity(String path) {
        getForList(path, (request, response) ->
                roadmapService.findRoadmapsAndScenariosByRatedEntity(
                        getEntityReference(request)));
    }


    private void registerAddScenario(String path) {
        postForDatum(path, (request, resp) -> {
            ensureUserHasAdminRights(request);
            return roadmapService.addScenario(
                    getId(request),
                    request.body(),
                    getUsername(request));
        });
    }


    private void registerUpdateName(String path) {
        postForDatum(path, (request, resp) -> {
            ensureUserHasAdminRights(request);
            return roadmapService.updateName(
                    getId(request),
                    request.body(),
                    getUsername(request));
        });
    }


    private void registerUpdateDescription(String path) {
        postForDatum(path, (req, resp) -> {
            ensureUserHasAdminRights(req);
            return roadmapService.updateDescription(
                    getId(req),
                    req.body(),
                    getUsername(req));
        });
    }


    private void registerUpdateLifecycleStatus(String path) {
        postForDatum(path, (request, resp) -> {
            ensureUserHasAdminRights(request);
            EntityLifecycleStatus entityLifecycleStatus = EnumUtilities.readEnum(request.body(), EntityLifecycleStatus.class, s -> null);
            return roadmapService.updateLifecycleStatus(
                    getId(request),
                    entityLifecycleStatus,
                    getUsername(request));
        });
    }


    private void registerFindRoadmapsBySelector(String path) {
        postForList(path, (req, resp) ->
                roadmapService.findRoadmapsBySelector(readIdSelectionOptionsFromBody(req)));
    }


    private void registerGetRoadmapById(String path) {
        getForDatum(path, (req, resp) ->
                roadmapService.getById(getId(req)));
    }


    // -- helpers --

    private void ensureUserHasAdminRights(Request request) {
        requireRole(userRoleService, request, SystemRole.SCENARIO_ADMIN);
    }

}
