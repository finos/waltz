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

package com.khartec.waltz.web.endpoints.extracts;

import com.khartec.waltz.data.SelectorUtilities;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.schema.tables.Application;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.khartec.waltz.schema.Tables.APPLICATION;
import static com.khartec.waltz.schema.tables.AssetCost.ASSET_COST;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.WebUtilities.readIdSelectionOptionsFromBody;
import static spark.Spark.post;


@Service
public class ApplicationCostExtractor extends DirectQueryBasedDataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationCostExtractor.class);
    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();


    @Autowired
    public ApplicationCostExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {
        post(mkPath("data-extract", "app-cost", "by-selector"), (request, response) -> {
            IdSelectionOptions idSelectionOptions = readIdSelectionOptionsFromBody(request);
            Select<Record1<Long>> idSelector = applicationIdSelectorFactory.apply(idSelectionOptions);
            Condition condition =
                    Application.APPLICATION.ID.in(idSelector)
                            .and(SelectorUtilities.mkApplicationConditions(idSelectionOptions));
            SelectConditionStep<?> qry = prepareExtractQuery(condition);
            String fileName = String.format("app-costs-for-%s-%s",
                    idSelectionOptions.entityReference().kind().name().toLowerCase(),
                    idSelectionOptions.entityReference().id());
            LOG.debug("extracted app cost for entity ref {}", idSelectionOptions.entityReference());
            return writeExtract(fileName, qry, request, response);
        });
    }


    private SelectConditionStep<Record5<String, String, Integer, String, BigDecimal>> prepareExtractQuery(Condition condition) {

        Integer year = dsl
                .select(DSL.max(ASSET_COST.YEAR).as("latest"))
                .from(ASSET_COST)
                .fetchOne("latest", Integer.class);

        return dsl.select(APPLICATION.ASSET_CODE.as("Asset Code"),
                        APPLICATION.NAME.as("Name"),
                        ASSET_COST.YEAR.as("Year"),
                        ASSET_COST.KIND.as("Cost Type"),
                        ASSET_COST.AMOUNT.as("Amount"))
                .from(ASSET_COST)
                .innerJoin(Application.APPLICATION)
                .on(ASSET_COST.ASSET_CODE.eq(Application.APPLICATION.ASSET_CODE))
                .where(condition)
                .and(ASSET_COST.YEAR.eq(year));

    }
}
