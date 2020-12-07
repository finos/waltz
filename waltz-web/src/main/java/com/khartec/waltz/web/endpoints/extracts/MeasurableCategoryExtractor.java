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


import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.Tables.ENTITY_HIERARCHY;
import static com.khartec.waltz.schema.Tables.INVOLVEMENT;
import static com.khartec.waltz.schema.tables.InvolvementKind.INVOLVEMENT_KIND;
import static com.khartec.waltz.schema.tables.Measurable.MEASURABLE;
import static com.khartec.waltz.schema.tables.MeasurableCategory.MEASURABLE_CATEGORY;
import static com.khartec.waltz.schema.tables.Person.PERSON;
import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static spark.Spark.get;


@Service
public class MeasurableCategoryExtractor extends DirectQueryBasedDataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(MeasurableCategoryExtractor.class);


    @Autowired
    public MeasurableCategoryExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {

        String path = mkPath("data-extract", "measurable-category", ":id");
        get(path, (request, response) -> {
            long categoryId = getId(request);
            String categoryName = dsl
                    .select(MEASURABLE_CATEGORY.NAME)
                    .from(MEASURABLE_CATEGORY)
                    .where(MEASURABLE_CATEGORY.ID.eq(categoryId))
                    .fetchOne(MEASURABLE_CATEGORY.NAME);

            checkNotNull(categoryName, "category cannot be null");
            String suggestedFilename = categoryName
                    .replace(".", "-")
                    .replace(" ", "-")
                    .replace(",", "-");

            SelectConditionStep<Record> data = dsl
                    .select(
                            MEASURABLE.ID.as("Id"),
                            MEASURABLE.PARENT_ID.as("Parent Id"),
                            MEASURABLE.EXTERNAL_ID.as("External Id"),
                            ENTITY_HIERARCHY.LEVEL.as("Level"),
                            MEASURABLE.NAME.as("Name"),
                            MEASURABLE.DESCRIPTION.as("Description"))
                    .select(INVOLVEMENT_KIND.NAME.as("Role"))
                    .select(PERSON.DISPLAY_NAME.as("Person"),
                            PERSON.EMAIL.as("Email"))
                    .from(MEASURABLE)
                    .innerJoin(ENTITY_HIERARCHY)
                        .on(ENTITY_HIERARCHY.ID.eq(MEASURABLE.ID))
                        .and(ENTITY_HIERARCHY.ANCESTOR_ID.eq(MEASURABLE.ID))
                        .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.MEASURABLE.name()))
                    .leftJoin(INVOLVEMENT)
                        .on(INVOLVEMENT.ENTITY_ID.eq(MEASURABLE.ID).and(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.MEASURABLE.name())))
                    .leftJoin(INVOLVEMENT_KIND)
                        .on(INVOLVEMENT_KIND.ID.eq(INVOLVEMENT.KIND_ID))
                    .leftJoin(PERSON)
                        .on(PERSON.EMPLOYEE_ID.eq(INVOLVEMENT.EMPLOYEE_ID))
                    .where(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(categoryId))
                    .and(MEASURABLE.ENTITY_LIFECYCLE_STATUS.eq(EntityLifecycleStatus.ACTIVE.name()));

            return writeExtract(
                    suggestedFilename,
                    data,
                    request,
                    response);
        });
    }

}
