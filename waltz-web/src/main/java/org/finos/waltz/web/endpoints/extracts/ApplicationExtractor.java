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

import org.finos.waltz.schema.tables.Application;
import org.finos.waltz.data.SelectorUtilities;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.web.WebUtilities;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record8;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.schema.Tables.APPLICATION;
import static org.finos.waltz.schema.Tables.ORGANISATIONAL_UNIT;
import static spark.Spark.post;


// TODO: remove in 1.61 if not needed
@Service
@Deprecated
public class ApplicationExtractor extends DirectQueryBasedDataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationExtractor.class);
    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();


    @Autowired
    public ApplicationExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {
        post(WebUtilities.mkPath("data-extract", "application", "by-selector-old"), (request, response) -> {
            IdSelectionOptions idSelectionOptions = WebUtilities.readIdSelectionOptionsFromBody(request);
            Select<Record1<Long>> idSelector = applicationIdSelectorFactory.apply(idSelectionOptions);
            Condition condition =
                    Application.APPLICATION.ID.in(idSelector)
                            .and(SelectorUtilities.mkApplicationConditions(idSelectionOptions));
            SelectConditionStep<?> qry = prepareExtractQuery(condition);
            String fileName = String.format("application-for-%s-%s",
                    idSelectionOptions.entityReference().kind().name().toLowerCase(),
                    idSelectionOptions.entityReference().id());
            LOG.debug("extracted applications for entity ref {}", idSelectionOptions.entityReference());
            return writeExtract(fileName, qry, request, response);
        });
    }


    private SelectConditionStep<Record8<Long, String, String, String, String, String, String, String>> prepareExtractQuery(Condition condition) {

        return dsl
                .selectDistinct(
                        APPLICATION.ID.as("Waltz Id"),
                        APPLICATION.NAME.as("Name"),
                        APPLICATION.ASSET_CODE.as("Asset Code"),
                        ORGANISATIONAL_UNIT.NAME.as("Org Unit"),
                        APPLICATION.KIND.as("Application Kind"),
                        APPLICATION.OVERALL_RATING.as("Overall Rating"),
                        APPLICATION.BUSINESS_CRITICALITY.as("Business Criticality"),
                        APPLICATION.LIFECYCLE_PHASE.as("Lifecycle Phase"))
                .from(APPLICATION)
                .innerJoin(ORGANISATIONAL_UNIT)
                .on(ORGANISATIONAL_UNIT.ID.eq(APPLICATION.ORGANISATIONAL_UNIT_ID))
                .where(condition);

    }
}
