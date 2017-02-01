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
import com.khartec.waltz.service.perspective_definition.PerspectiveDefinitionService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


@Service
public class PerspectiveDefinitionEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "perspective-definition");


    private final PerspectiveDefinitionService perspectiveDefinitionService;

    
    @Autowired
    public PerspectiveDefinitionEndpoint(PerspectiveDefinitionService perspectiveDefinitionService) {
        checkNotNull(perspectiveDefinitionService, "perspectiveDefinitionService cannot be null");
        this.perspectiveDefinitionService = perspectiveDefinitionService;
    }


    @Override
    public void register() {
        ListRoute<PerspectiveDefinition> findAllRoute = (request, response) -> perspectiveDefinitionService.findAll();

        getForList(BASE_URL, findAllRoute);
    }

}
