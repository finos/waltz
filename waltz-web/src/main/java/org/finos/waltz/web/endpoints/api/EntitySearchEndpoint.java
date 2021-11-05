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

import org.finos.waltz.service.entity_search.EntitySearchService;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.finos.waltz.model.entity_search.ImmutableEntitySearchOptions;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class EntitySearchEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "entity-search");
    private final EntitySearchService entitySearchService;


    @Autowired
    public EntitySearchEndpoint(EntitySearchService entitySearchService) {
        checkNotNull(entitySearchService, "entitySearchService cannot be null");

        this.entitySearchService = entitySearchService;
    }


    @Override
    public void register() {

        String searchPath = WebUtilities.mkPath(BASE_URL);

        ListRoute<EntityReference> searchRoute = (request, response) ->  {
            String username = WebUtilities.getUsername(request);
            EntitySearchOptions entitySearchOptions = WebUtilities.readBody(request, EntitySearchOptions.class);

            return entitySearchService.search(
                    ImmutableEntitySearchOptions
                            .copyOf(entitySearchOptions)
                            .withUserId(username));
        };

        EndpointUtilities.postForList(searchPath, searchRoute);
    }

}
