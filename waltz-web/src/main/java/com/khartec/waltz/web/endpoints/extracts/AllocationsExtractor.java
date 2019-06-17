/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.web.endpoints.extracts;


import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.web.WebUtilities.*;
import static spark.Spark.post;



@Service
public class AllocationsExtractor extends BaseDataExtractor{

    private final ApplicationIdSelectorFactory applicationIdSelectorFactory;


    @Autowired
    public AllocationsExtractor(DSLContext dsl, ApplicationIdSelectorFactory applicationIdSelectorFactory) {
        super(dsl);
        this.applicationIdSelectorFactory = applicationIdSelectorFactory;
    }


    @Override
    public void register() {
        registerExtractForAll( mkPath("data-extract", "allocations", "all"));
        registerExtractForCategory( mkPath("data-extract", "allocations", "measurable-category", ":measurableCategoryId"));
        registerExtractForScheme( mkPath("data-extract", "allocations", "allocation-scheme", ":schemeId"));
    }


    private void registerExtractForAll(String path) {
        post(path, (request, response) -> {
            ApplicationIdSelectionOptions applicationIdSelectionOptions = readAppIdSelectionOptionsFromBody(request);
            SelectConditionStep<Record> qry = prepareQuery(
                    DSL.trueCondition(),
                    applicationIdSelectionOptions);

            return writeExtract(
                    "all_allocations",
                    qry,
                    request,
                    response);
        });
    }


    private void registerExtractForCategory(String path) {
        post(path, (request, response) -> {

            long measurableCategoryId = getLong(request, "measurableCategoryId");

            ApplicationIdSelectionOptions applicationIdSelectionOptions = readAppIdSelectionOptionsFromBody(request);

            Record1<String> fileName = dsl
                    .select(DSL.concat(MEASURABLE_CATEGORY.NAME, "_all_allocations"))
                    .from(MEASURABLE_CATEGORY)
                    .where(MEASURABLE_CATEGORY.ID.eq(measurableCategoryId))
                    .fetchOne();

            SelectConditionStep<Record> qry = prepareQuery(
                    MEASURABLE.MEASURABLE_CATEGORY_ID.eq(measurableCategoryId),
                    applicationIdSelectionOptions);

            return writeExtract(
                    fileName.value1(),
                    qry,
                    request,
                    response);
        });
    }


    private void registerExtractForScheme(String path) {
        post(path, (request, response) -> {

            long schemeId = getLong(request, "schemeId");

            ApplicationIdSelectionOptions applicationIdSelectionOptions = readAppIdSelectionOptionsFromBody(request);

            Record2<String, String> fileNameInfoRow = dsl.select(MEASURABLE_CATEGORY.NAME, ALLOCATION_SCHEME.NAME)
                    .from(ALLOCATION_SCHEME)
                    .innerJoin(MEASURABLE_CATEGORY).on(ALLOCATION_SCHEME.MEASURABLE_CATEGORY_ID.eq(MEASURABLE_CATEGORY.ID))
                    .where(ALLOCATION_SCHEME.ID.eq(schemeId))
                    .fetchOne();

            SelectConditionStep<Record> qry = prepareQuery(
                    ALLOCATION_SCHEME.ID.eq(schemeId),
                    applicationIdSelectionOptions);

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
                                                     ApplicationIdSelectionOptions applicationIdSelectionOptions) {
        Select<Record1<Long>> appSelector = applicationIdSelectorFactory.apply(applicationIdSelectionOptions);
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

        Condition condition = MEASURABLE_RATING.ENTITY_ID.in(appSelector)
                .and(MEASURABLE_RATING.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
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
                .leftJoin(ALLOCATION).on(
                        ALLOCATION.ALLOCATION_SCHEME_ID.eq(ALLOCATION_SCHEME.ID)
                                .and(ALLOCATION.MEASURABLE_ID.eq(MEASURABLE_RATING.MEASURABLE_ID))
                                .and(ALLOCATION.ENTITY_ID.eq(MEASURABLE_RATING.ENTITY_ID))
                                .and(ALLOCATION.ENTITY_KIND.eq(MEASURABLE_RATING.ENTITY_KIND)))
                .where(condition);
    }
}
