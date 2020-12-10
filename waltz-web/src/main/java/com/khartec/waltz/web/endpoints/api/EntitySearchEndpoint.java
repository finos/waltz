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
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.entity_search.ImmutableEntitySearchOptions;
import com.khartec.waltz.service.entity_search.EntitySearchService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;

@Service
public class EntitySearchEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "entity-search");
    private final EntitySearchService entitySearchService;


    @Autowired
    public EntitySearchEndpoint(EntitySearchService entitySearchService) {
        checkNotNull(entitySearchService, "entitySearchService cannot be null");

        this.entitySearchService = entitySearchService;
    }


    @Override
    public void register() {

        String searchPath = mkPath(BASE_URL);

        ListRoute<EntityReference> searchRoute = (request, response) ->  {
            String username = getUsername(request);
            EntitySearchOptions entitySearchOptions = readBody(request, EntitySearchOptions.class);

            return entitySearchService.search(
                    ImmutableEntitySearchOptions
                            .copyOf(entitySearchOptions)
                            .withUserId(username));
        };

        postForList(searchPath, searchRoute);
    }

}
