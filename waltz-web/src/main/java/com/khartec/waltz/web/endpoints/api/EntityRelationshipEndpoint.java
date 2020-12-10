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

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.common.StreamUtilities;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.entity_relationship.*;
import com.khartec.waltz.service.entity_relationship.EntityRelationshipService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.EnumUtilities.readEnum;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class EntityRelationshipEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "entity-relationship");

    private final EntityRelationshipService entityRelationshipService;

    @Autowired
    public EntityRelationshipEndpoint(EntityRelationshipService entityRelationshipService) {
        checkNotNull(entityRelationshipService, "entityRelationshipService cannot be null");

        this.entityRelationshipService = entityRelationshipService;
    }


    @Override
    public void register() {
        String findForEntityPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String exactMatchPath = mkPath(BASE_URL, "relationship", ":aKind", ":aId", ":bKind", ":bId", ":relationshipKind");

        DatumRoute<Boolean> removeRelationshipRoute = (req, resp) -> {
            EntityRelationshipKey entityRelationshipKey = ImmutableEntityRelationshipKey
                    .builder()
                    .a(readEntityA(req))
                    .b(readEntityB(req))
                    .relationshipKind(readRelationshipKind(req))
                    .build();

            return entityRelationshipService.removeRelationship(entityRelationshipKey);
        };

        ListRoute<EntityRelationship> findForEntityRoute = (req, resp) -> {
            EntityReference ref = getEntityReference(req);

            Directionality directionality = readEnum(
                    req.queryParams("directionality"),
                    Directionality.class,
                    (s) -> Directionality.ANY);

            List<RelationshipKind> relationshipKinds = parseRelationshipKindParams(req);

            return entityRelationshipService.findForEntity(ref, directionality, relationshipKinds);
        };

        DatumRoute<Boolean> createRelationshipRoute = (req, resp) -> {
            EntityRelationship entityRelationship = ImmutableEntityRelationship
                    .builder()
                    .a(readEntityA(req))
                    .b(readEntityB(req))
                    .relationship(readRelationshipKind(req))
                    .lastUpdatedBy(getUsername(req))
                    .lastUpdatedAt(DateTimeUtilities.nowUtc())
                    .provenance("waltz")
                    .build();

            return entityRelationshipService.createRelationship(entityRelationship);
        };

        getForList(findForEntityPath, findForEntityRoute);
        deleteForDatum(exactMatchPath, removeRelationshipRoute);
        postForDatum(exactMatchPath, createRelationshipRoute);
    }

    private String readRelationshipKind(Request req) {
        String relationshipKind = req.params("relationshipKind");
        checkNotNull(relationshipKind, "relationshipKind cannot be parsed");
        return relationshipKind;
    }

    private EntityReference readEntityA(Request req) {
        return getEntityReference(req, "aKind", "aId");
    }

    private EntityReference readEntityB(Request req) {
        return getEntityReference(req, "bKind", "bId");
    }

    private List<RelationshipKind> parseRelationshipKindParams(Request req) {
        return StreamUtilities.ofNullableArray(req.queryParamsValues("relationshipKind"))
                .map(p -> readEnum(p, RelationshipKind.class, (s) -> null))
                .filter(rk -> rk != null)
                .collect(Collectors.toList());
    }
}
