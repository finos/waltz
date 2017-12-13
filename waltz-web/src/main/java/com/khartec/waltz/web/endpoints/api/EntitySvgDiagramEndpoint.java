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

import com.khartec.waltz.model.entity_svg_diagram.EntitySvgDiagram;
import com.khartec.waltz.service.entity_svg_diagram.EntitySvgDiagramService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getEntityReference;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


@Service
public class EntitySvgDiagramEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "entity-svg-diagram");
    private final EntitySvgDiagramService entitySvgDiagramService;


    @Autowired
    public EntitySvgDiagramEndpoint(EntitySvgDiagramService entitySvgDiagramService) {
        checkNotNull(entitySvgDiagramService, "entitySvgDiagramService cannot be null");
        this.entitySvgDiagramService = entitySvgDiagramService;
    }


    @Override
    public void register() {

        String findByEntityPath = mkPath(BASE_URL, "entity-ref", ":kind", ":id");

        ListRoute<EntitySvgDiagram> findByEntityRoute = (request, response) ->
                entitySvgDiagramService.findForEntityReference(getEntityReference(request));

        getForList(findByEntityPath, findByEntityRoute);
    }

}
