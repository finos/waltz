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

import org.finos.waltz.service.custom_environment.CustomEnvironmentService;
import org.finos.waltz.service.permission.PermissionGroupService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.custom_environment.CustomEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class CustomEnvironmentEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "custom-environment");

    private final CustomEnvironmentService customEnvironmentService;
    private final PermissionGroupService permissionGroupService;


    @Autowired
    public CustomEnvironmentEndpoint(CustomEnvironmentService customEnvironmentService,
                                     PermissionGroupService permissionGroupService) {
        this.customEnvironmentService = customEnvironmentService;
        this.permissionGroupService = permissionGroupService;
    }


    @Override
    public void register() {

        String findAllPath = mkPath(BASE_URL, "all");
        String findByOwningEntityReferencePath = mkPath(BASE_URL, "owning-entity", "kind", ":kind", "id", ":id");
        String createPath = mkPath(BASE_URL, "create");
        String deletePath = mkPath(BASE_URL, "remove", "id", ":id");


        ListRoute<CustomEnvironment> findAllRoute = (request, response) -> customEnvironmentService.findAll();

        DatumRoute<Long> createRoute = (request, response) -> {
            CustomEnvironment env = readBody(request, CustomEnvironment.class);
            String username = getUsername(request);

            return customEnvironmentService.create(env, username);
        };

        DatumRoute<Boolean> deleteRoute = (request, response) -> {
            String username = getUsername(request);
            long envId = getId(request);
            return customEnvironmentService.remove(envId, username);
        };

        ListRoute<CustomEnvironment> findByOwningEntityReferenceRoute = (request, response) -> {
            EntityReference ref = getEntityReference(request);
            return customEnvironmentService.findByOwningEntityRef(ref);
        };


        getForList(findAllPath, findAllRoute);
        getForList(findByOwningEntityReferencePath, findByOwningEntityReferenceRoute);

        postForDatum(createPath, createRoute);
        deleteForDatum(deletePath, deleteRoute);
    }

}
