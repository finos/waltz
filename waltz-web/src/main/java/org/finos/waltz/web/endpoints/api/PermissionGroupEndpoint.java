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


import org.finos.waltz.model.attestation.UserAttestationPermission;
import org.finos.waltz.model.permission_group.Permission;
import org.finos.waltz.service.permission.PermissionGroupService;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForList;


@Service
public class PermissionGroupEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "permission-group");

    private final PermissionGroupService permissionGroupService;

    @Autowired
    public PermissionGroupEndpoint(PermissionGroupService permissionGroupService) {
        this.permissionGroupService = permissionGroupService;
    }


    @Override
    public void register() {

        String findPermissionsForParentEntityRefPath = mkPath(
                BASE_URL,
                "entity-ref",
                ":kind",
                ":id");

        String findSupportedMeasurableCategoryAttestationsPath = mkPath(
                BASE_URL,
                "entity-ref",
                ":kind",
                ":id",
                "supported-attestations",
                "measurable-category");

        ListRoute<UserAttestationPermission> findSupportedMeasurableCategoryAttestationsRoute = ((request, response) ->
                permissionGroupService.findSupportedMeasurableCategoryAttestations(
                    getEntityReference(request),
                    getUsername(request)));

        ListRoute<Permission> findPermissionsForParentEntityRefRoute = (request, response) ->
            permissionGroupService.findPermissionsForParentReference(
                    getEntityReference(request),
                    getUsername(request));

        getForList(findPermissionsForParentEntityRefPath, findPermissionsForParentEntityRefRoute);
        getForList(findSupportedMeasurableCategoryAttestationsPath, findSupportedMeasurableCategoryAttestationsRoute);
    }

}
