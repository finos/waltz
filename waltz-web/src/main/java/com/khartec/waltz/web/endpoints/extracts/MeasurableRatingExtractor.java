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


import com.khartec.waltz.data.InlineSelectFieldFactory;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.schema.Tables;
import com.khartec.waltz.schema.tables.*;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.SetUtilities.asSet;
import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.schema.tables.Measurable.MEASURABLE;
import static com.khartec.waltz.schema.tables.MeasurableRating.MEASURABLE_RATING;
import static com.khartec.waltz.web.WebUtilities.*;
import static org.jooq.tools.StringUtils.toCamelCase;
import static spark.Spark.post;


@Service
public class MeasurableRatingExtractor extends DirectQueryBasedDataExtractor {

    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();
    private final Measurable m = MEASURABLE.as("m");
    private final Application app = APPLICATION.as("app");
    private final MeasurableCategory mc = Tables.MEASURABLE_CATEGORY.as("mc");
    private final MeasurableRating mr = MEASURABLE_RATING.as("mr");
    private final MeasurableRatingPlannedDecommission mrd = MEASURABLE_RATING_PLANNED_DECOMMISSION.as("mrd");
    private final MeasurableRatingReplacement mrr = MEASURABLE_RATING_REPLACEMENT.as("mrr");
    private final RatingScheme rs = RATING_SCHEME.as("rs");
    private final RatingSchemeItem rsi = RATING_SCHEME_ITEM.as("rsi");
    private final Field<String> replacementAppName = InlineSelectFieldFactory.mkNameField(mrr.ENTITY_ID, mrr.ENTITY_KIND, asSet(EntityKind.APPLICATION));
    private final Field<String> replacementAppExtId = InlineSelectFieldFactory.mkExternalIdField(mrr.ENTITY_ID, mrr.ENTITY_KIND, asSet(EntityKind.APPLICATION));



    @Autowired
    public MeasurableRatingExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {

        String path = mkPath("data-extract", "measurable-rating", ":id");
        registerAllocations(path);

        String unmappedPath = mkPath("data-extract", "measurable-rating", "unmapped", ":id");
        registerUnmappedAllocations(unmappedPath);
    }

    private void registerAllocations(String path) {
        post(path, (request, response) -> {
            long categoryId = getId(request);
            IdSelectionOptions selectionOpts = readIdSelectionOptionsFromBody(request);
            Select<Record1<Long>> appSelector = applicationIdSelectorFactory.apply(selectionOpts);


            SelectSelectStep<Record> reportColumns = dsl
                    .select(m.NAME.as("Taxonomy Item Name"),
                            m.EXTERNAL_ID.as("Taxonomy Item Code"))
                    .select(app.NAME.as("App Name"),
                            app.ASSET_CODE.as("App Code"),
                            app.ID.as("App Waltz Id"),
                            app.KIND.as("App Kind"))
                    .select(rsi.NAME.as("Rating Name"),
                            rsi.CODE.as("Rating Code"))
                    .select(mr.DESCRIPTION.as("Rating Description"),
                            mr.LAST_UPDATED_AT.as("Last Updated Time"),
                            mr.LAST_UPDATED_BY.as("Last Updated By"))
                    .select(mrd.PLANNED_DECOMMISSION_DATE.as("Planned Decommission Date"))
                    .select(replacementAppName.as("Replacement App Name"),
                            replacementAppExtId.as("Replacement External Id"),
                            mrr.PLANNED_COMMISSION_DATE.as("Replacement Commission Date"));

            Condition reportConditions = app.ENTITY_LIFECYCLE_STATUS.eq(EntityLifecycleStatus.ACTIVE.name())
                    .and(m.MEASURABLE_CATEGORY_ID.eq(categoryId))
                    .and(m.ENTITY_LIFECYCLE_STATUS.ne(EntityLifecycleStatus.REMOVED.name()))
                    .and(mr.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                    .and(mr.ENTITY_ID.in(appSelector))
                    .and(rsi.CODE.eq(mr.RATING));

            SelectConditionStep<Record> qry = reportColumns
                    .from(m)
                    .innerJoin(mr).on(mr.MEASURABLE_ID.eq(m.ID))
                    .innerJoin(app).on(app.ID.eq(mr.ENTITY_ID))
                    .innerJoin(mc).on(mc.ID.eq(m.MEASURABLE_CATEGORY_ID))
                    .innerJoin(rs).on(rs.ID.eq(mc.RATING_SCHEME_ID))
                    .innerJoin(rsi).on(rsi.SCHEME_ID.eq(rs.ID))
                    .leftJoin(mrd)
                        .on(mrd.MEASURABLE_ID.eq(mr.MEASURABLE_ID)
                                .and(mrd.ENTITY_ID.eq(mr.ENTITY_ID))
                                .and(mrd.ENTITY_KIND.eq(mr.ENTITY_KIND)))
                    .leftJoin(mrr).on(mrr.DECOMMISSION_ID.eq(mrd.ID))
                    .where(reportConditions);

            String categoryName = dsl.select(mc.NAME).from(mc).where(mc.ID.eq(categoryId)).fetchOne(mc.NAME);
            String suggestedFilename = toCamelCase(categoryName);

            return writeExtract(
                    suggestedFilename,
                    qry,
                    request,
                    response);
        });
    }

    private void registerUnmappedAllocations(String path) {
        post(path, (request, response) -> {
            long categoryId = getId(request);

            IdSelectionOptions selectionOpts = readIdSelectionOptionsFromBody(request);
            Select<Record1<Long>> appIds = applicationIdSelectorFactory.apply(selectionOpts);

            SelectConditionStep<Record1<Long>> appIdsAssignedToAnyMeasurableInTheCategory = dsl
                    .selectDistinct(MEASURABLE_RATING.ENTITY_ID)
                    .from(MEASURABLE_RATING)
                    .join(MEASURABLE)
                    .on(MEASURABLE.ID.eq(MEASURABLE_RATING.MEASURABLE_ID))
                    .where(MEASURABLE_RATING.ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                            .and(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(categoryId)));

            SelectSeekStep1<Record4<String, String, Long, String>, String> qry = dsl
                    .selectDistinct(app.NAME.as("App Name"),
                            app.ASSET_CODE.as("App Code"),
                            app.ID.as("App Waltz Id"),
                            app.KIND.as("App Kind"))
                    .from(app)
                    .where(app.ID.in(appIds))
                    .and(app.ID.notIn(appIdsAssignedToAnyMeasurableInTheCategory))
                    .orderBy(app.NAME);

            String categoryName = dsl.select(mc.NAME).from(mc).where(mc.ID.eq(categoryId)).fetchOne(mc.NAME);
            String suggestedFilename = toCamelCase(categoryName) + "-Unmapped-Applications";

            return writeExtract(
                    suggestedFilename,
                    qry,
                    request,
                    response);
        });
    }

}
