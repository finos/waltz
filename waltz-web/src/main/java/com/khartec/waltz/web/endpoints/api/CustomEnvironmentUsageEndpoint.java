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
import com.khartec.waltz.model.custom_environment.CustomEnvironmentUsage;
import com.khartec.waltz.model.custom_environment.CustomEnvironmentUsageInfo;
import com.khartec.waltz.service.custom_environment.CustomEnvironmentUsageService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class CustomEnvironmentUsageEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "custom-environment-usage");

    private final CustomEnvironmentUsageService customEnvironmentUsageService;


    @Autowired
    public CustomEnvironmentUsageEndpoint(CustomEnvironmentUsageService customEnvironmentUsageService) {
        this.customEnvironmentUsageService = customEnvironmentUsageService;
    }


    @Override
    public void register() {

        String addAssetPath = mkPath(BASE_URL, "add");
        String removePath = mkPath(BASE_URL, "remove", "id", ":id");
        String findUsageByOwningEntityReferencePath = mkPath(BASE_URL, "owning-entity", "kind", ":kind", "id", ":id");



        DatumRoute<Long> addAssetRoute = (request, response) -> {
//            requireAnyRole(userRoleService, request, SystemRole.USER_ADMIN, SystemRole.ADMIN);
            CustomEnvironmentUsage usage = readBody(request, CustomEnvironmentUsage.class);
            String username = getUsername(request);
            return customEnvironmentUsageService.addAsset(usage, username);
        };

        DatumRoute<Boolean> removeRoute = (request, response) -> {
//            requireAnyRole(userRoleService, request, SystemRole.USER_ADMIN, SystemRole.ADMIN);
            String username = getUsername(request);
            long usageId = getId(request);
            return customEnvironmentUsageService.remove(usageId, username);
        };

        ListRoute<CustomEnvironmentUsageInfo> findUsageByOwningEntityReferenceRoute = (request, response) -> {
            EntityReference ref = getEntityReference(request);
            return customEnvironmentUsageService.findUsageInfoByOwningEntity(ref);
        };


        getForList(findUsageByOwningEntityReferencePath, findUsageByOwningEntityReferenceRoute);

        postForDatum(addAssetPath, addAssetRoute);
        deleteForDatum(removePath, removeRoute);
    }

}
