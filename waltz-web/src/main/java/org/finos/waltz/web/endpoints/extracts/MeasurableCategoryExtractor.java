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


import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.EntityHierarchy;
import org.finos.waltz.schema.tables.Measurable;
import org.finos.waltz.web.WebUtilities;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.SelectOnConditionStep;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.schema.Tables.ENTITY_HIERARCHY;
import static org.finos.waltz.schema.Tables.INVOLVEMENT;
import static org.finos.waltz.schema.tables.InvolvementKind.INVOLVEMENT_KIND;
import static org.finos.waltz.schema.tables.Measurable.MEASURABLE;
import static org.finos.waltz.schema.tables.MeasurableCategory.MEASURABLE_CATEGORY;
import static org.finos.waltz.schema.tables.Person.PERSON;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static spark.Spark.get;


@Service
public class MeasurableCategoryExtractor extends DirectQueryBasedDataExtractor {


    @Autowired
    public MeasurableCategoryExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {
        String flatPath = WebUtilities.mkPath("data-extract", "measurable-category", "flat", ":id");
        get(flatPath, (request, response) -> {
            long categoryId = WebUtilities.getId(request);

            EntityHierarchy eh = ENTITY_HIERARCHY.as("eh");
            Measurable m = Tables.MEASURABLE.as("m");
            Field<Integer> maxLevelField = DSL.max(eh.LEVEL).as("maxLevel");
            Integer maxLevel = dsl
                    .select(maxLevelField)
                    .from(eh)
                    .where(eh.KIND.eq(EntityKind.MEASURABLE.name()))
                    .and(eh.ID.in(DSL
                            .select(m.ID)
                            .from(m)
                            .where(m.MEASURABLE_CATEGORY_ID.eq(categoryId))
                            .and(m.ENTITY_LIFECYCLE_STATUS.ne(EntityLifecycleStatus.REMOVED.name()))))
                    .fetchOne(maxLevelField);

            ArrayList<Tuple3<Integer, EntityHierarchy, Measurable>> ehAndMeasurableTablesForLevel = new ArrayList<>(maxLevel);

            for (int i = 0; i < maxLevel; i++) {
                ehAndMeasurableTablesForLevel.add(i, tuple(i + 1, ENTITY_HIERARCHY.as("eh" + i), Tables.MEASURABLE.as("l" + i)));
            }

            List<Field<?>> fields = ehAndMeasurableTablesForLevel
                    .stream()
                    .flatMap(t -> Stream.of(
                            _m(t).NAME.as("Level " + t.v1 + " Name"),
                            _m(t).EXTERNAL_ID.as("Level " + t.v1 + " External Id"),
                            _m(t).ID.as("Level " + t.v1 + " Waltz Id")))
                    .collect(Collectors.toList());

            Tuple3<Integer, EntityHierarchy, Measurable> l1 = ehAndMeasurableTablesForLevel.get(0);

            SelectOnConditionStep<Record> qry = dsl
                    .select(fields)
                    .from(_m(l1))
                    .innerJoin(_eh(l1))
                    .on(_eh(l1).ID.eq(_m(l1).ID)
                            .and(_eh(l1).KIND.eq(EntityKind.MEASURABLE.name()))
                            .and(_eh(l1).DESCENDANT_LEVEL.eq(_lvl(l1)))
                            .and(_m(l1).MEASURABLE_CATEGORY_ID.eq(categoryId))
                            .and(_m(l1).ENTITY_LIFECYCLE_STATUS.ne(EntityLifecycleStatus.REMOVED.name())));

            for (int i = 1; i < maxLevel; i++) {
                Tuple3<Integer, EntityHierarchy, Measurable> curr = ehAndMeasurableTablesForLevel.get(i);
                Tuple3<Integer, EntityHierarchy, Measurable> prev = ehAndMeasurableTablesForLevel.get(i - 1);
                qry = qry
                        .leftJoin(_eh(curr))
                        .on(_eh(curr).ANCESTOR_ID.eq(_eh(prev).ID)
                                .and(_eh(curr).KIND.eq(EntityKind.MEASURABLE.name())
                                        .and(_eh(curr).DESCENDANT_LEVEL.eq(_lvl(curr)))))
                        .leftJoin(_m(curr))
                        .on(_m(curr).ID.eq(_eh(curr).ID)
                                .and(_m(curr).ENTITY_LIFECYCLE_STATUS.ne(EntityLifecycleStatus.REMOVED.name())));
            }

            return writeExtract(
                    mkSuggestedFilename(categoryId),
                    qry,
                    request,
                    response);
        });




        String parentChildPath = WebUtilities.mkPath("data-extract", "measurable-category", ":id");
        get(parentChildPath, (request, response) -> {
            long categoryId = WebUtilities.getId(request);
            String suggestedFilename = mkSuggestedFilename(categoryId);

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

    private String mkSuggestedFilename(long categoryId) {
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
        return suggestedFilename;
    }


    private static Measurable _m(Tuple3<Integer, EntityHierarchy, Measurable> l1) {
        return l1.v3;
    }

    private static EntityHierarchy _eh(Tuple3<Integer, EntityHierarchy, Measurable> l1) {
        return l1.v2;
    }

    private static Integer _lvl(Tuple3<Integer, EntityHierarchy, Measurable> l1) {
        return l1.v1;
    }
}
