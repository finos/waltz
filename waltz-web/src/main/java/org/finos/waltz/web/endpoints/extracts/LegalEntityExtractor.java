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

import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.web.WebUtilities;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

import static java.lang.String.format;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.finos.waltz.schema.Tables.*;
import static org.finos.waltz.schema.tables.SoftwareUsage.SOFTWARE_USAGE;
import static org.finos.waltz.schema.tables.SoftwareVersionLicence.SOFTWARE_VERSION_LICENCE;
import static spark.Spark.get;


@Service
public class LegalEntityExtractor extends DirectQueryBasedDataExtractor {

    @Autowired
    public LegalEntityExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {
        String path = WebUtilities.mkPath("data-extract", "legal-entity", "target-ref", ":kind", ":id");
        get(path, (request, response) -> {

            EntityReference entityRef = WebUtilities.getEntityReference(request);

            SelectConditionStep<Record9<Long, String, String, String, String, String, Timestamp, String, String>> qry = dsl
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
                            .and(LEGAL_ENTITY_RELATIONSHIP.TARGET_ID.eq(entityRef.id()))));

            String filename = format("legal-entities-%s/%s", entityRef.kind(), entityRef.id());

            return writeExtract(
                    filename,
                    qry,
                    request,
                    response);
        });
    }
}
