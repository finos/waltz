/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
            requireAnyRole(userRoleService, request, SystemRole.USER_ADMIN, SystemRole.ADMIN);
            return roleService.findAllRoles();
        };

        DatumRoute<Boolean> createCustomRoleRoute = (request, response) -> {
            requireAnyRole(userRoleService, request, SystemRole.USER_ADMIN, SystemRole.ADMIN);
            Map<String, String> body = (Map<String, String>) readBody(request, Map.class);
            String roleName = body.get("name");
            String description = body.get("description");
            return roleService.create(roleName, description);
        };


        // --- register
        postForDatum(BASE_URL, createCustomRoleRoute);
        getForDatum(BASE_URL, findAllRolesRoute);
    }

}
