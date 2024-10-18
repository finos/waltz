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

import org.finos.waltz.common.EnumUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.bulk_upload.BulkUpdateMode;
import org.finos.waltz.model.bulk_upload.entity_relationship.BulkUploadRelationshipApplyResult;
import org.finos.waltz.model.bulk_upload.entity_relationship.BulkUploadRelationshipValidationResult;
import org.finos.waltz.service.entity_relationship.BulkUploadRelationshipService;
import org.finos.waltz.service.entity_relationship.EntityRelationshipService;
import org.finos.waltz.service.measurable_rating.BulkMeasurableItemParser;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.StreamUtilities;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.entity_relationship.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.stream.Collectors;

import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.EnumUtilities.readEnum;

@Service
public class EntityRelationshipEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "entity-relationship");

    private final EntityRelationshipService entityRelationshipService;

    private final BulkUploadRelationshipService bulkUploadRelationshipService;

    @Autowired
    public EntityRelationshipEndpoint(EntityRelationshipService entityRelationshipService,
                                      BulkUploadRelationshipService bulkUploadRelationshipService) {
        checkNotNull(entityRelationshipService, "entityRelationshipService cannot be null");
        checkNotNull(bulkUploadRelationshipService, "bulkUploadRelationshipService cannot be null");

        this.entityRelationshipService = entityRelationshipService;
        this.bulkUploadRelationshipService = bulkUploadRelationshipService;
    }


    @Override
    public void register() {
        String getByIdPath = mkPath(BASE_URL, "id", ":id");
        String findForEntityPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String bulkPreview = mkPath(BASE_URL, "bulk", "preview", ":id");
        String bulkApply = mkPath(BASE_URL, "bulk", "apply", ":id");
        String exactMatchPath = mkPath(BASE_URL, "relationship", ":aKind", ":aId", ":bKind", ":bId", ":relationshipKind");

        registerPreviewBulkUploadRelationship(bulkPreview);
        registerApplyBulkUploadRelationship(bulkApply);

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

        DatumRoute<EntityRelationship> getByIdRoute = (req, resp) -> entityRelationshipService.getById(getId(req));;

        getForDatum(getByIdPath, getByIdRoute);
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

    private void registerPreviewBulkUploadRelationship(String path) {
        postForDatum(path, (req, resp) -> {
            Long relationshipKindId = Long.parseLong(req.params("id"));
            String body = req.body();
            return bulkUploadRelationshipService.bulkPreview(body, relationshipKindId);
        });
    }

    private void registerApplyBulkUploadRelationship(String path) {
        postForDatum(path, (req, resp) -> {
            Long relationshipKindId = Long.parseLong(req.params("id"));
            String body = req.body();
            String user = getUsername(req);
            BulkUploadRelationshipValidationResult previewResult = bulkUploadRelationshipService.bulkPreview(body, relationshipKindId);
            BulkUploadRelationshipApplyResult applyResult = bulkUploadRelationshipService.bulkApply(previewResult, relationshipKindId, user);
            return applyResult;
        });
    }
}
