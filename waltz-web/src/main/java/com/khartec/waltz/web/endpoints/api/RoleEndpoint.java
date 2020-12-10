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

import com.khartec.waltz.model.role.Role;
import com.khartec.waltz.model.user.*;
import com.khartec.waltz.service.role.RoleService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class RoleEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "role");

    private final RoleService roleService;
    private final UserRoleService userRoleService;


    @Autowired
    public RoleEndpoint(RoleService roleService,
                        UserRoleService userRoleService) {
        this.roleService = roleService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {

        DatumRoute<Set<Role>> findAllRolesRoute = (request, response) -> {
            return roleService.findAllRoles();
        };

        DatumRoute<Boolean> createCustomRoleRoute = (request, response) -> {
            requireAnyRole(userRoleService, request, SystemRole.USER_ADMIN, SystemRole.ADMIN);
            Map<String, String> body = (Map<String, String>) readBody(request, Map.class);
            String key = body.get("key");
            String roleName = body.get("name");
            String description = body.get("description");
            return roleService.create(key, roleName, description);
        };


        // --- register
        postForDatum(BASE_URL, createCustomRoleRoute);
        getForDatum(BASE_URL, findAllRolesRoute);
    }

}
