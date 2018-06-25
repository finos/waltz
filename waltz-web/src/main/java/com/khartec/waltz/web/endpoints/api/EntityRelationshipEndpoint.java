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

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.common.StreamUtilities;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.entity_relationship.*;
import com.khartec.waltz.service.entity_relationship.EntityRelationshipService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.EnumUtilities.readEnum;
import static com.khartec.waltz.web.WebUtilities.getEntityReference;
import static com.khartec.waltz.web.WebUtilities.getUsername;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.deleteForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForDatum;

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

    private RelationshipKind readRelationshipKind(Request req) {
        RelationshipKind relationshipKind = WebUtilities.readEnum(
                req,
                "relationshipKind",
                RelationshipKind.class,
                (s) -> null);
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
