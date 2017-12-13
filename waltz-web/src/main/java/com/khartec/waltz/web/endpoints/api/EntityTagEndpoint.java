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
import com.khartec.waltz.service.entity_tag.EntityTagService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;


@Service
public class EntityTagEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "entity-tag");
    private final EntityTagService entityTagService;


    @Autowired
    public EntityTagEndpoint(EntityTagService entityTagService) {
        checkNotNull(entityTagService, "entityTagService cannot be null");
        this.entityTagService = entityTagService;
    }

    @Override
    public void register() {
        ListRoute<String> findAllTagsRoute = (request, response)
                -> entityTagService.findAllTags();

        ListRoute<EntityReference> findByTagRoute = (request, response)
                -> entityTagService.findByTag(request.body());

        ListRoute<String> updateRoute = (req, resp) -> {
            String username = getUsername(req);
            EntityReference ref = getEntityReference(req);
            List<String> tags = readStringsFromBody(req);
            return entityTagService.updateTags(ref, tags, username);
        };

        ListRoute<String> findTagsForEntityReference = (req, resp) -> {
            EntityReference ref = getEntityReference(req);
            return entityTagService.findTagsForEntityReference(ref);
        };

        getForList(mkPath(BASE_URL, "tags"), findAllTagsRoute);
        postForList(mkPath(BASE_URL, "tags"), findByTagRoute);
        postForList(mkPath(BASE_URL, "entity", ":kind", ":id"), updateRoute);
        getForList(mkPath(BASE_URL, "entity", ":kind", ":id"), findTagsForEntityReference);
    }

}
