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
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.web.WebUtilities;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.SelectConditionStep;
import org.jooq.SelectSelectStep;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.finos.waltz.schema.Tables.ALLOCATION;
import static org.finos.waltz.schema.Tables.ALLOCATION_SCHEME;
import static org.finos.waltz.schema.Tables.APPLICATION;
import static org.finos.waltz.schema.Tables.ENTITY_HIERARCHY;
import static org.finos.waltz.schema.Tables.MEASURABLE;
import static org.finos.waltz.schema.Tables.MEASURABLE_CATEGORY;
import static org.finos.waltz.schema.Tables.MEASURABLE_RATING;
import static org.finos.waltz.schema.Tables.ORGANISATIONAL_UNIT;
import static org.finos.waltz.schema.Tables.RATING_SCHEME_ITEM;
import static org.finos.waltz.web.WebUtilities.mkPath;
import static spark.Spark.get;
import static spark.Spark.post;



@Service
public class AllocationsExtractor extends DirectQueryBasedDataExtractor {

    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();


    @Autowired
    public AllocationsExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {
        registerExtractForAll(mkPath("data-extract", "allocations", "all"));
        registerExtractForCategory(mkPath("data-extract", "allocations", "measurable-category", ":measurableCategoryId"));
        registerExtractForMeasurable(mkPath("data-extract", "allocations", "measurable", ":id"));
        registerExtractForScheme(mkPath("data-extract", "allocations", "allocation-scheme", ":schemeId"));
    }


    private void registerExtractForAll(String path) {
        post(path, (request, response) -> {
            IdSelectionOptions idSelectionOptions = WebUtilities.readIdSelectionOptionsFromBody(request);
            SelectConditionStep<Record> qry = prepareQuery(
                    DSL.trueCondition(),
                    Optional.of(idSelectionOptions));

            return writeExtract(
                    "all_allocations",
                    qry,
                    request,
                    response);
        });
    }


    private void registerExtractForMeasurable(String path) {
        get(path, (request, response) -> {

            long measurableId = WebUtilities.getId(request);

            Record1<String> fileName = dsl
                    .select(DSL.concat(MEASURABLE.NAME, "_all_allocations"))
                    .from(MEASURABLE)
                    .where(MEASURABLE.ID.eq(measurableId))
                    .fetchOne();

            SelectConditionStep<Record> qry = prepareQuery(
                    MEASURABLE.ID.in(DSL
                            .select(ENTITY_HIERARCHY.ID)
                            .from(ENTITY_HIERARCHY)
                            .where(ENTITY_HIERARCHY.KIND.eq(EntityKind.MEASURABLE.name())
                                    .and(ENTITY_HIERARCHY.ANCESTOR_ID.eq(measurableId)))),
                    Optional.empty());

            return writeExtract(
                    fileName.value1(),
                    qry,
                    request,
                    response);
        });
    }


    private void registerExtractForCategory(String path) {
        post(path, (request, response) -> {

            long measurableCategoryId = WebUtilities.getLong(request, "measurableCategoryId");

            IdSelectionOptions applicationIdSelectionOptions = WebUtilities.readIdSelectionOptionsFromBody(request);

            Record1<String> fileName = dsl
                    .select(DSL.concat(MEASURABLE_CATEGORY.NAME, "_all_allocations"))
                    .from(MEASURABLE_CATEGORY)
                    .where(MEASURABLE_CATEGORY.ID.eq(measurableCategoryId))
                    .fetchOne();

            SelectConditionStep<Record> qry = prepareQuery(
                    MEASURABLE.MEASURABLE_CATEGORY_ID.eq(measurableCategoryId),
                    Optional.of(applicationIdSelectionOptions));

            return writeExtract(
                    fileName.value1(),
                    qry,
                    request,
                    response);
        });
    }


    private void registerExtractForScheme(String path) {
        post(path, (request, response) -> {

            long schemeId = WebUtilities.getLong(request, "schemeId");

            IdSelectionOptions applicationIdSelectionOptions = WebUtilities.readIdSelectionOptionsFromBody(request);

            Record2<String, String> fileNameInfoRow = dsl
                    .select(MEASURABLE_CATEGORY.NAME, ALLOCATION_SCHEME.NAME)
                    .from(ALLOCATION_SCHEME)
                    .innerJoin(MEASURABLE_CATEGORY).on(ALLOCATION_SCHEME.MEASURABLE_CATEGORY_ID.eq(MEASURABLE_CATEGORY.ID))
                    .where(ALLOCATION_SCHEME.ID.eq(schemeId))
                    .fetchOne();

            SelectConditionStep<Record> qry = prepareQuery(
                    ALLOCATION_SCHEME.ID.eq(schemeId),
                    Optional.of(applicationIdSelectionOptions));

            String filename = fileNameInfoRow.value1() + "_" + fileNameInfoRow.value2();

            return writeExtract(
                    filename,
                    qry,
                    request,
                    response);
        });
    }


    // -- HELPER ----

    private SelectConditionStep<Record> prepareQuery(Condition additionalCondition,
                                                     Optional<IdSelectionOptions> idSelectionOptions) {
        SelectSelectStep<Record> reportColumns = dsl
                .select(APPLICATION.NAME.as("Application Name"),
                        APPLICATION.ID.as("Application Waltz Id"),
                        APPLICATION.ASSET_CODE.as("Application Asset Code"),
                        APPLICATION.OVERALL_RATING.as("Application Rating"))
                .select(ORGANISATIONAL_UNIT.NAME.as("Organisational Unit"))
                .select(MEASURABLE_CATEGORY.NAME.as("Taxonomy Category Name"))
                .select(MEASURABLE.NAME.as("Taxonomy Item Name"),
                        MEASURABLE.ID.as("Taxonomy Item Waltz Id"),
                        MEASURABLE.EXTERNAL_ID.as("Taxonomy Item External Id"))
                .select(MEASURABLE_RATING.RATING.as("Taxonomy Item Rating"),
                        DSL
                                .when(MEASURABLE_CATEGORY.ALLOW_PRIMARY_RATINGS.isTrue().and(MEASURABLE_RATING.IS_PRIMARY.isTrue()), "Y")
                                .when(MEASURABLE_CATEGORY.ALLOW_PRIMARY_RATINGS.isTrue().and(MEASURABLE_RATING.IS_PRIMARY.isFalse()), "N")
                                .otherwise("n/a").as("Is Primary"),
                        MEASURABLE_RATING.DESCRIPTION.as("Taxonomy Item Rating Description"))
                .select(RATING_SCHEME_ITEM.NAME.as("Taxonomy Item Rating Name"))
                .select(ENTITY_HIERARCHY.LEVEL.as("Taxonomy Item Hierarchy Level"))
                .select(MEASURABLE_RATING.LAST_UPDATED_AT.as("Rating Last Updated"),
                        MEASURABLE_RATING.LAST_UPDATED_BY.as("Rating Last Updated By"))
                .select(DSL.coalesce(ALLOCATION_SCHEME.NAME, "").as("Allocation Scheme"))
                .select(DSL.coalesce(ALLOCATION.ALLOCATION_PERCENTAGE, 0).as("Allocation Percentage"),
                        ALLOCATION.LAST_UPDATED_AT.as("Allocation Last Updated"),
                        DSL.coalesce(ALLOCATION.LAST_UPDATED_BY, "").as("Allocation Last Updated By"),
                        DSL.coalesce(ALLOCATION.PROVENANCE, "").as("Allocation Provenance"));

        Condition appCondition = idSelectionOptions
                .map(applicationIdSelectorFactory)
                .map(selector -> MEASURABLE_RATING.ENTITY_ID.in(selector)
                        .and(MEASURABLE_RATING.ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .orElse(DSL.trueCondition());

        Condition condition = appCondition
                .and(ENTITY_HIERARCHY.ID.eq(ENTITY_HIERARCHY.ANCESTOR_ID))
                .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.MEASURABLE.name()))
                .and(ENTITY_HIERARCHY.ID.eq(MEASURABLE_RATING.MEASURABLE_ID))
                .and(RATING_SCHEME_ITEM.SCHEME_ID.eq(MEASURABLE_CATEGORY.RATING_SCHEME_ID)) //
                .and(RATING_SCHEME_ITEM.CODE.eq(MEASURABLE_RATING.RATING)) // y
                .and(additionalCondition);

        return reportColumns
                .from(MEASURABLE_RATING)
                .innerJoin(MEASURABLE).on(MEASURABLE_RATING.MEASURABLE_ID.eq(MEASURABLE.ID))
                .innerJoin(ENTITY_HIERARCHY).on(MEASURABLE.ID.eq(ENTITY_HIERARCHY.ID))
                .innerJoin(APPLICATION).on(MEASURABLE_RATING.ENTITY_ID.eq(APPLICATION.ID))
                .innerJoin(ORGANISATIONAL_UNIT).on(APPLICATION.ORGANISATIONAL_UNIT_ID.eq(ORGANISATIONAL_UNIT.ID))
                .innerJoin(MEASURABLE_CATEGORY).on(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(MEASURABLE_CATEGORY.ID))
                .innerJoin(RATING_SCHEME_ITEM).on(MEASURABLE_CATEGORY.RATING_SCHEME_ID.eq(RATING_SCHEME_ITEM.SCHEME_ID))
                .leftJoin(ALLOCATION_SCHEME).on(ALLOCATION_SCHEME.MEASURABLE_CATEGORY_ID.eq(MEASURABLE_CATEGORY.ID))
                .leftJoin(ALLOCATION)
                .on(ALLOCATION.ALLOCATION_SCHEME_ID.eq(ALLOCATION_SCHEME.ID)
                        .and(ALLOCATION.MEASURABLE_RATING_ID.eq(MEASURABLE_RATING.ID)))
                .where(condition);
    }
}
