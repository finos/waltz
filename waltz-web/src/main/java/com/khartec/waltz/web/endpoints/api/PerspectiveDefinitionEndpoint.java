/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import com.khartec.waltz.model.perspective.PerspectiveDefinition;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.perspective_definition.PerspectiveDefinitionService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.WebUtilities.readBody;
import static com.khartec.waltz.web.WebUtilities.requireRole;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;


@Service
public class PerspectiveDefinitionEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "perspective-definition");

    private final UserRoleService userRoleService;
    private final PerspectiveDefinitionService perspectiveDefinitionService;

    
    @Autowired
    public PerspectiveDefinitionEndpoint(UserRoleService userRoleService, PerspectiveDefinitionService perspectiveDefinitionService) {
        checkNotNull(userRoleService, "userRoleService cannot be null");
        checkNotNull(perspectiveDefinitionService, "perspectiveDefinitionService cannot be null");

        this.userRoleService = userRoleService;
        this.perspectiveDefinitionService = perspectiveDefinitionService;
    }


    @Override
    public void register() {
        ListRoute<PerspectiveDefinition> findAllRoute = (request, response) -> perspectiveDefinitionService.findAll();

        getForList(BASE_URL, findAllRoute);
        postForList(BASE_URL, this::createRoute);
    }


    private Collection<PerspectiveDefinition> createRoute(Request request, Response response) throws IOException {
        requireRole(userRoleService, request, Role.ADMIN);
        PerspectiveDefinition perspectiveDefinition = readBody(request, PerspectiveDefinition.class);
        perspectiveDefinitionService.create(perspectiveDefinition);
        return perspectiveDefinitionService.findAll();
    }

}
