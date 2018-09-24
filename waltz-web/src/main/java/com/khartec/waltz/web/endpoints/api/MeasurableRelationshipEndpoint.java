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

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.entity_relationship.*;
import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.measurable_relationship.MeasurableRelationshipService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import java.util.Map;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class MeasurableRelationshipEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(MeasurableRelationshipEndpoint.class);
    private static final String BASE_URL = WebUtilities.mkPath("api", "measurable-relationship");

    private final MeasurableRelationshipService measurableRelationshipService;
    private final UserRoleService userRoleService;


    @Autowired
    public MeasurableRelationshipEndpoint(MeasurableRelationshipService measurableRelationshipService, UserRoleService userRoleService) {
        checkNotNull(measurableRelationshipService, "measurableRelationshipService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.measurableRelationshipService = measurableRelationshipService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {

        String findForEntityReferencePath = mkPath(BASE_URL, ":kind", ":id");
        String tallyForEntityReferencePath = mkPath(BASE_URL, ":kind", ":id", "tally");
        String removeRelationshipPath = mkPath(BASE_URL, ":kindA", ":idA", ":kindB", ":idB", ":relationshipKind");
        String updateRelationshipPath = mkPath(BASE_URL, ":kindA", ":idA", ":kindB", ":idB", ":relationshipKind");
        String createRelationshipPath = mkPath(BASE_URL, ":kindA", ":idA", ":kindB", ":idB", ":relationshipKind");


        ListRoute<EntityRelationship> findForEntityReferenceRoute = (request, response)
                -> measurableRelationshipService.findForEntityReference(getEntityReference(request));

        DatumRoute<Map<EntityKind, Integer>> tallyForEntityReferenceRoute = (request, response)
                -> measurableRelationshipService.tallyForEntityReference(getEntityReference(request));

        DatumRoute<Boolean> removeRelationshipRoute = (request, response) ->{
            requireRole(userRoleService, request, Role.CAPABILITY_EDITOR);
            EntityRelationshipKey key = readRelationshipKeyFromRequest(request);
            return measurableRelationshipService.remove(key, getUsername(request));
        };

        DatumRoute<Boolean> createRelationshipRoute = (request, response) -> {
            requireRole(userRoleService, request, Role.CAPABILITY_EDITOR);

            String userName = getUsername(request);
            EntityReference entityRefA = getEntityReferenceA(request);
            EntityReference entityRefB = getEntityReferenceB(request);
            RelationshipKind relationshipKind = getRelationshipKind(request);
            String description = request.body();

            return measurableRelationshipService.create(
                    userName,
                    entityRefA,
                    entityRefB,
                    relationshipKind,
                    description);
        };

        DatumRoute<Boolean> updateRelationshipRoute = (request, response) -> {
            requireRole(userRoleService, request, Role.CAPABILITY_EDITOR);
            EntityRelationshipKey key = readRelationshipKeyFromRequest(request);
            UpdateEntityRelationshipParams params = readBody(request, UpdateEntityRelationshipParams.class);
            return measurableRelationshipService.update(key, params, getUsername(request));
        };


        getForList(findForEntityReferencePath, findForEntityReferenceRoute);
        getForDatum(tallyForEntityReferencePath, tallyForEntityReferenceRoute);
        deleteForDatum(removeRelationshipPath, removeRelationshipRoute);
        putForDatum(updateRelationshipPath, updateRelationshipRoute);
        postForDatum(createRelationshipPath, createRelationshipRoute);
    }


    // --- HELPERS ---

    private EntityRelationshipKey readRelationshipKeyFromRequest(Request request) {
        return ImmutableEntityRelationshipKey.builder()
                        .a(getEntityReferenceA(request))
                        .b(getEntityReferenceB(request))
                        .relationshipKind(getRelationshipKind(request))
                        .build();
    }


    private RelationshipKind getRelationshipKind(Request request) {
        return readEnum(
                request,
                "relationshipKind",
                RelationshipKind.class,
                s -> RelationshipKind.RELATES_TO);
    }


    private EntityReference getEntityReferenceA(Request request) {
        return getReference("A", request);
    }


    private EntityReference getEntityReferenceB(Request request) {
        return getReference("B", request);
    }


    private EntityReference getReference(String qualifier, Request request) {
        EntityKind kind = readEnum(
                request,
                "kind" + qualifier,
                EntityKind.class,
                s -> null);
        long id = getLong(request, "id" + qualifier);
        return mkRef(kind, id);
    }

}
