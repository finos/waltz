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

import org.finos.waltz.model.role.Role;
import org.finos.waltz.model.role.RoleView.RoleView;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.service.role.RoleService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import static org.finos.waltz.web.WebUtilities.getId;
import static org.finos.waltz.web.WebUtilities.mkPath;
import static org.finos.waltz.web.WebUtilities.readBody;
import static org.finos.waltz.web.WebUtilities.requireAnyRole;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForList;
import static org.finos.waltz.web.endpoints.EndpointUtilities.postForDatum;


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

        ListRoute<Role> findAllRolesRoute = (request, response) -> roleService.findAllRoles();

        DatumRoute<Long> createCustomRoleRoute = (request, response) -> {
            requireAnyRole(userRoleService, request, SystemRole.USER_ADMIN, SystemRole.ADMIN);
            Map<String, String> body = (Map<String, String>) readBody(request, Map.class);
            String key = body.get("key");
            String roleName = body.get("name");
            String description = body.get("description");
            return roleService.create(key, roleName, description);
        };

        DatumRoute<RoleView> getRoleViewRoute = (request, response) -> roleService.getRoleView(getId(request));


        // --- register
        postForDatum(BASE_URL, createCustomRoleRoute);
        getForList(BASE_URL, findAllRolesRoute);
        getForDatum(mkPath(BASE_URL, "view", ":id"), getRoleViewRoute);
    }

}
