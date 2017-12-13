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

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.entity_search.ImmutableEntitySearchOptions;
import com.khartec.waltz.service.entity_search.EntitySearchService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;

@Service
public class EntitySearchEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(EntitySearchEndpoint.class);
    private static final String BASE_URL = mkPath("api", "entity-search");
    private final EntitySearchService entitySearchService;


    @Autowired
    public EntitySearchEndpoint(EntitySearchService entitySearchService) {
        checkNotNull(entitySearchService, "entitySearchService cannot be null");

        this.entitySearchService = entitySearchService;
    }


    @Override
    public void register() {

        String searchPath = mkPath(BASE_URL, ":query");

        ListRoute<EntityReference> searchRoute = (request, response) ->  {
            String username = getUsername(request);
            EntitySearchOptions entitySearchOptions = readBody(request, EntitySearchOptions.class);

            String query = request.params("query");

            return entitySearchService.search(
                    query,
                    ImmutableEntitySearchOptions
                            .copyOf(entitySearchOptions)
                            .withUserId(username));
        };

        postForList(searchPath, searchRoute);
    }

}
