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


import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.taxonomy_management.TaxonomyChangeLifecycleStatus;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record8;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Set;

import static org.finos.waltz.common.Checks.checkTrue;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.schema.tables.MeasurableCategory.MEASURABLE_CATEGORY;
import static org.finos.waltz.schema.tables.TaxonomyChange.TAXONOMY_CHANGE;
import static org.finos.waltz.web.WebUtilities.getKind;
import static org.finos.waltz.web.WebUtilities.getLong;
import static org.finos.waltz.web.WebUtilities.mkPath;
import static spark.Spark.get;


@Service
public class TaxonomyChangeExtractor extends DirectQueryBasedDataExtractor
        implements SupportsJsonExtraction {


    private static final Field<String> PRIMARY_REF_NAME = InlineSelectFieldFactory.mkNameField(
            TAXONOMY_CHANGE.PRIMARY_REFERENCE_ID,
            TAXONOMY_CHANGE.PRIMARY_REFERENCE_KIND,
            asSet(EntityKind.MEASURABLE, EntityKind.DATA_TYPE));

    private static final Set<EntityKind> supportedKinds = SetUtilities.asSet(
            EntityKind.MEASURABLE_CATEGORY,
            EntityKind.DATA_TYPE);

    @Autowired
    public TaxonomyChangeExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {
        String allAppliedPath = mkPath("data-extract", "taxonomy-changes", "all-applied", ":domainKind", ":domainId");
        get(allAppliedPath, (request, response) -> {
            EntityKind domainKind = getKind(request, "domainKind");
            Long domainId = getLong(request, "domainId");

            checkTrue(
                    supportedKinds.contains(domainKind),
                    "Cannot extract taxonomy changes for domain: %s",
                    domainKind);

            String filenameStem = domainKind == EntityKind.DATA_TYPE
                    ? "data-type-taxonomy-changes"
                    : dsl
                        .select(DSL.concat(MEASURABLE_CATEGORY.NAME, "-taxonomy-changes").as("name"))
                        .from(MEASURABLE_CATEGORY)
                        .where(MEASURABLE_CATEGORY.ID.eq(domainId))
                        .fetchOne("name", String.class);

            return writeExtract(
                    filenameStem,
                    prepareExtract(domainKind, domainId),
                    request,
                    response);
        });

    }


    private SelectConditionStep<Record8<String, String, String, String, Timestamp, String, Timestamp, String>> prepareExtract(EntityKind domainKind,
                                                                                                                              Long domainId) {
        return  dsl
                .select(TAXONOMY_CHANGE.CHANGE_TYPE.as("Change Type"),
                        PRIMARY_REF_NAME.as("Entity"),
                        TAXONOMY_CHANGE.PARAMS.as("Parameters"),
                        TAXONOMY_CHANGE.LAST_UPDATED_BY.as("Updated By"),
                        TAXONOMY_CHANGE.LAST_UPDATED_AT.as("Updated At"),
                        TAXONOMY_CHANGE.CREATED_BY.as("Created By"),
                        TAXONOMY_CHANGE.CREATED_AT.as("Created At"),
                        TAXONOMY_CHANGE.DESCRIPTION.as("Description"))
                .from(TAXONOMY_CHANGE)
                .where(TAXONOMY_CHANGE.DOMAIN_KIND.eq(domainKind.name()))
                .and(domainId == null
                        ? DSL.trueCondition()
                        : TAXONOMY_CHANGE.DOMAIN_ID.eq(domainId))
                .and(TAXONOMY_CHANGE.STATUS.eq(TaxonomyChangeLifecycleStatus.EXECUTED.name()));
    }

}
