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


import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.web.WebUtilities;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.schema.Tables.COMPLEXITY;
import static org.finos.waltz.schema.Tables.COMPLEXITY_KIND;
import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static spark.Spark.post;


@Service
public class ComplexityExtractor extends DirectQueryBasedDataExtractor {

    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();
    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();

    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            COMPLEXITY.ENTITY_ID,
            COMPLEXITY.ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION))
            .as("entity_name");


    @Autowired
    public ComplexityExtractor(DSLContext dsl) {
        super(dsl);
        checkNotNull(applicationIdSelectorFactory, "applicationIdSelectorFactory cannot be null");
    }


    @Override
    public void register() {

        String findBySelectorPath = WebUtilities.mkPath("data-extract", "complexity", "target-kind", ":kind", "selector");

        post(findBySelectorPath, (request, response) -> {
            IdSelectionOptions idSelectionOptions = WebUtilities.readIdSelectionOptionsFromBody(request);
            EntityKind targetKind = WebUtilities.getKind(request);
            GenericSelector genericSelector = genericSelectorFactory.applyForKind(targetKind, idSelectionOptions);

            SelectConditionStep<Record> qry = dsl
                    .select(ENTITY_NAME_FIELD.as("Entity Name"))
                    .select(COMPLEXITY.ENTITY_ID)
                    .select(COMPLEXITY_KIND.NAME.as("Complexity Kind"))
                    .select(COMPLEXITY.SCORE.as("Score"))
                    .select(COMPLEXITY.PROVENANCE)
                    .from(COMPLEXITY)
                    .innerJoin(COMPLEXITY_KIND).on(COMPLEXITY.COMPLEXITY_KIND_ID.eq(COMPLEXITY_KIND.ID))
                    .where(COMPLEXITY.ENTITY_ID.in(genericSelector.selector())
                            .and(COMPLEXITY.ENTITY_KIND.eq(genericSelector.kind().name())));

            return writeExtract(
                    mkFilename(idSelectionOptions),
                    qry,
                    request,
                    response
            );

        });
    }


    private String mkFilename(IdSelectionOptions idSelectionOptions) {
        return format("%s-%d-complexity-scores", idSelectionOptions.entityReference().kind(), idSelectionOptions.entityReference().id());
    }

}
