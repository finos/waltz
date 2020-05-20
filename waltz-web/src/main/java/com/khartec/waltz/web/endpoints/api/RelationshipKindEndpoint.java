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
import com.khartec.waltz.model.rel.RelationshipKind;
import com.khartec.waltz.service.relationship_kind.RelationshipKindService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getEntityReference;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

@Service
public class RelationshipKindEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "relationship-kind");

    private final RelationshipKindService relationshipKindService;

    @Autowired
    public RelationshipKindEndpoint(RelationshipKindService relationshipKindService) {
        checkNotNull(relationshipKindService, "relationshipKindService cannot be null");

        this.relationshipKindService = relationshipKindService;
    }


    @Override
    public void register() {
        String findRelationshipKindsBetweenEntitesPath = mkPath(BASE_URL, "entities", ":aKind", ":aId", ":bKind", ":bId");

        ListRoute<RelationshipKind> findAllRoute = (req, resp) -> relationshipKindService.findAll();

        ListRoute<RelationshipKind> findRelationshipKindsBetweenEntitesRoute = (req, resp) ->
                relationshipKindService.findRelationshipKindsBetweenEntites(readEntityA(req), readEntityB(req));

        getForList(BASE_URL, findAllRoute);
        getForList(findRelationshipKindsBetweenEntitesPath, findRelationshipKindsBetweenEntitesRoute);
    }


    private EntityReference readEntityA(Request req) {
        return getEntityReference(req, "aKind", "aId");
    }


    private EntityReference readEntityB(Request req) {
        return getEntityReference(req, "bKind", "bId");
    }
}
