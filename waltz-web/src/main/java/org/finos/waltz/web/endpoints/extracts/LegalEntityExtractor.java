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

package org.finos.waltz.web.endpoints.extracts;

import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.web.WebUtilities;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

import static java.lang.String.format;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.finos.waltz.schema.Tables.*;
import static org.finos.waltz.schema.tables.SoftwareUsage.SOFTWARE_USAGE;
import static org.finos.waltz.schema.tables.SoftwareVersionLicence.SOFTWARE_VERSION_LICENCE;
import static spark.Spark.get;


@Service
public class LegalEntityExtractor extends DirectQueryBasedDataExtractor {


    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
                    LEGAL_ENTITY_RELATIONSHIP.TARGET_ID,
                    LEGAL_ENTITY_RELATIONSHIP.TARGET_KIND,
                    newArrayList(EntityKind.APPLICATION))
            .as("entity_name");

    private static final Field<String> ENTITY_EXT_ID_FIELD = InlineSelectFieldFactory.mkExternalIdField(
                    LEGAL_ENTITY_RELATIONSHIP.TARGET_ID,
                    LEGAL_ENTITY_RELATIONSHIP.TARGET_KIND,
                    newArrayList(EntityKind.APPLICATION))
            .as("entity_external_id");

    @Autowired
    public LegalEntityExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {
        String findByTargetRefPath = WebUtilities.mkPath("data-extract", "legal-entity", "target-ref", ":kind", ":id");
        String findByLegalEntityIdPath = WebUtilities.mkPath("data-extract", "legal-entity", "id", ":id");
        String findByRelKindIdPath = WebUtilities.mkPath("data-extract", "legal-entity", "relationships", "kind", ":id");

        get(findByTargetRefPath, (request, response) -> {

            EntityReference entityRef = WebUtilities.getEntityReference(request);

            SelectSeekStep2<Record9<Long, String, String, String, String, String, Timestamp, String, String>, String, String> qry = dsl
                    .selectDistinct(
                            LEGAL_ENTITY.ID.as("Legal Entity Id"),
                            LEGAL_ENTITY.NAME.as("Legal Entity Name"),
                            LEGAL_ENTITY.DESCRIPTION.as("Legal Entity Description"),
                            LEGAL_ENTITY.EXTERNAL_ID.as("Legal Entity External Id"),
                            LEGAL_ENTITY_RELATIONSHIP_KIND.NAME.as("Relationship Kind"),
                            LEGAL_ENTITY_RELATIONSHIP.DESCRIPTION.as("Comment"),
                            LEGAL_ENTITY_RELATIONSHIP.LAST_UPDATED_AT.as("Last Updated At"),
                            LEGAL_ENTITY_RELATIONSHIP.LAST_UPDATED_BY.as("Last Updated By"),
                            LEGAL_ENTITY_RELATIONSHIP.PROVENANCE.as("Provenance"))
                    .from(LEGAL_ENTITY_RELATIONSHIP)
                    .innerJoin(LEGAL_ENTITY_RELATIONSHIP_KIND)
                    .on(LEGAL_ENTITY_RELATIONSHIP_KIND.ID.eq(LEGAL_ENTITY_RELATIONSHIP.RELATIONSHIP_KIND_ID)
                            .and(LEGAL_ENTITY_RELATIONSHIP_KIND.TARGET_KIND.eq(LEGAL_ENTITY_RELATIONSHIP.TARGET_KIND)))
                    .innerJoin(LEGAL_ENTITY)
                    .on(LEGAL_ENTITY.ID.eq(LEGAL_ENTITY_RELATIONSHIP.LEGAL_ENTITY_ID))
                    .where(dsl.renderInlined(LEGAL_ENTITY_RELATIONSHIP.TARGET_KIND.eq(entityRef.kind().name())
                            .and(LEGAL_ENTITY_RELATIONSHIP.TARGET_ID.eq(entityRef.id()))))
                    .orderBy(LEGAL_ENTITY.NAME, LEGAL_ENTITY_RELATIONSHIP_KIND.NAME);

            String filename = format("legal-entities-%s/%s", entityRef.kind(), entityRef.id());

            return writeExtract(
                    filename,
                    qry,
                    request,
                    response);
        });

        get(findByLegalEntityIdPath, (request, response) -> {

            Long id = WebUtilities.getId(request);

            String legalEntityName = dsl
                    .select(LEGAL_ENTITY.NAME)
                    .from(LEGAL_ENTITY)
                    .where(LEGAL_ENTITY.ID.eq(id))
                    .fetchOne(LEGAL_ENTITY.NAME);

            SelectSeekStep2<Record, String, String> qry = getRelationshipsForLegalEntity(id);

            String filename = format("%s-related-entities", legalEntityName);

            return writeExtract(
                    filename,
                    qry,
                    request,
                    response);
        });

        get(findByRelKindIdPath, (request, response) -> {

            Long id = WebUtilities.getId(request);

            SelectSeekStep2<Record, String, String> qry = getRelationshipsForRelationshipKind(id);

            return writeExtract(
                    "legal-entity-relationships",
                    qry,
                    request,
                    response);
        });
    }

    private SelectSeekStep2<Record, String, String> getRelationshipsForLegalEntity(Long id) {
        Field<String> targetEntityName = ENTITY_NAME_FIELD.as("Target Entity Name");
        return dsl
                .select(LEGAL_ENTITY_RELATIONSHIP.TARGET_ID.as("Target Entity Id"),
                        LEGAL_ENTITY_RELATIONSHIP.TARGET_KIND.as("Target Entity Kind"))
                .select(targetEntityName)
                .select(LEGAL_ENTITY_RELATIONSHIP_KIND.NAME.as("Relationship Kind"),
                        LEGAL_ENTITY_RELATIONSHIP.DESCRIPTION.as("Comment"),
                        LEGAL_ENTITY_RELATIONSHIP.LAST_UPDATED_AT.as("Last Updated At"),
                        LEGAL_ENTITY_RELATIONSHIP.LAST_UPDATED_BY.as("Last Updated By"),
                        LEGAL_ENTITY_RELATIONSHIP.PROVENANCE.as("Provenance"))
                .from(LEGAL_ENTITY_RELATIONSHIP)
                .innerJoin(LEGAL_ENTITY_RELATIONSHIP_KIND)
                .on(LEGAL_ENTITY_RELATIONSHIP_KIND.ID.eq(LEGAL_ENTITY_RELATIONSHIP.RELATIONSHIP_KIND_ID)
                        .and(LEGAL_ENTITY_RELATIONSHIP_KIND.TARGET_KIND.eq(LEGAL_ENTITY_RELATIONSHIP.TARGET_KIND)))
                .innerJoin(LEGAL_ENTITY)
                .on(LEGAL_ENTITY.ID.eq(LEGAL_ENTITY_RELATIONSHIP.LEGAL_ENTITY_ID))
                .where(dsl.renderInlined(LEGAL_ENTITY_RELATIONSHIP.LEGAL_ENTITY_ID.eq(id)))
                .orderBy(targetEntityName, LEGAL_ENTITY_RELATIONSHIP_KIND.NAME);
    }

    private SelectSeekStep2<Record, String, String> getRelationshipsForRelationshipKind(Long id) {

        Field<String> targetEntityName = ENTITY_NAME_FIELD.as("Target Entity Name");
        Field<String> targetEntityExtId = ENTITY_EXT_ID_FIELD.as("Target Entity External Id");

        return dsl
                .select(LEGAL_ENTITY_RELATIONSHIP.TARGET_ID.as("Target Entity Id"),
                        LEGAL_ENTITY_RELATIONSHIP.TARGET_KIND.as("Target Entity Kind"))
                .select(targetEntityName)
                .select(targetEntityExtId)
                .select(LEGAL_ENTITY.ID.as("Legal Entity Id"),
                        LEGAL_ENTITY.NAME.as("Legal Entity Name"),
                        LEGAL_ENTITY.EXTERNAL_ID.as("Legal Entity External Id"))
                .select(LEGAL_ENTITY_RELATIONSHIP.DESCRIPTION.as("Comment"),
                        LEGAL_ENTITY_RELATIONSHIP.LAST_UPDATED_AT.as("Last Updated At"),
                        LEGAL_ENTITY_RELATIONSHIP.LAST_UPDATED_BY.as("Last Updated By"),
                        LEGAL_ENTITY_RELATIONSHIP.PROVENANCE.as("Provenance"))
                .from(LEGAL_ENTITY_RELATIONSHIP)
                .innerJoin(LEGAL_ENTITY_RELATIONSHIP_KIND)
                .on(LEGAL_ENTITY_RELATIONSHIP_KIND.ID.eq(LEGAL_ENTITY_RELATIONSHIP.RELATIONSHIP_KIND_ID)
                        .and(LEGAL_ENTITY_RELATIONSHIP_KIND.TARGET_KIND.eq(LEGAL_ENTITY_RELATIONSHIP.TARGET_KIND)))
                .innerJoin(LEGAL_ENTITY)
                .on(LEGAL_ENTITY.ID.eq(LEGAL_ENTITY_RELATIONSHIP.LEGAL_ENTITY_ID))
                .where(dsl.renderInlined(LEGAL_ENTITY_RELATIONSHIP.RELATIONSHIP_KIND_ID.eq(id)))
                .orderBy(targetEntityName, LEGAL_ENTITY_RELATIONSHIP_KIND.NAME);
    }
}
