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
