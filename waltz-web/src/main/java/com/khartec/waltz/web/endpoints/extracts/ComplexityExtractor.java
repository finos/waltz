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


import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.Tables.APPLICATION;
import static com.khartec.waltz.schema.Tables.COMPLEXITY_SCORE;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.WebUtilities.readIdSelectionOptionsFromBody;
import static spark.Spark.post;


@Service
public class ComplexityExtractor extends DirectQueryBasedDataExtractor {

    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();


    @Autowired
    public ComplexityExtractor(DSLContext dsl) {
        super(dsl);
        checkNotNull(applicationIdSelectorFactory, "applicationIdSelectorFactory cannot be null");
    }


    @Override
    public void register() {

        String path = mkPath("data-extract", "complexity", "all");
        post(path, (request, response) -> {
            IdSelectionOptions applicationIdSelectionOptions = readIdSelectionOptionsFromBody(request);
            Select<Record1<Long>> selector = applicationIdSelectorFactory.apply(applicationIdSelectionOptions);

            SelectConditionStep<Record4<String, String, String, BigDecimal>> qry = dsl
                    .select(APPLICATION.NAME.as("Application Name"),
                            APPLICATION.ASSET_CODE.as("Asset Code"),
                            COMPLEXITY_SCORE.COMPLEXITY_KIND.as("Complexity Kind"),
                            COMPLEXITY_SCORE.SCORE.as("Score"))
                    .from(COMPLEXITY_SCORE)
                    .innerJoin(APPLICATION)
                    .on(APPLICATION.ID.eq(COMPLEXITY_SCORE.ENTITY_ID))
                    .where(COMPLEXITY_SCORE.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                    .and(COMPLEXITY_SCORE.ENTITY_ID.in(selector));


            return writeExtract(
                    "complexity",
                    qry,
                    request,
                    response);
        });
    }

}
