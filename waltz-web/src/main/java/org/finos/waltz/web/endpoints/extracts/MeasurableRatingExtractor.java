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


import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.Application;
import org.finos.waltz.schema.tables.Measurable;
import org.finos.waltz.schema.tables.MeasurableCategory;
import org.finos.waltz.schema.tables.MeasurableRating;
import org.finos.waltz.schema.tables.MeasurableRatingPlannedDecommission;
import org.finos.waltz.schema.tables.MeasurableRatingReplacement;
import org.finos.waltz.schema.tables.OrganisationalUnit;
import org.finos.waltz.schema.tables.RatingScheme;
import org.finos.waltz.schema.tables.RatingSchemeItem;
import org.finos.waltz.web.WebUtilities;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record5;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.SelectSeekStep1;
import org.jooq.SelectSelectStep;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.schema.Tables.APPLICATION;
import static org.finos.waltz.schema.Tables.MEASURABLE_RATING_PLANNED_DECOMMISSION;
import static org.finos.waltz.schema.Tables.MEASURABLE_RATING_REPLACEMENT;
import static org.finos.waltz.schema.Tables.RATING_SCHEME;
import static org.finos.waltz.schema.Tables.RATING_SCHEME_ITEM;
import static org.finos.waltz.schema.tables.Measurable.MEASURABLE;
import static org.finos.waltz.schema.tables.MeasurableRating.MEASURABLE_RATING;
import static org.finos.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;
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
    private final OrganisationalUnit ou = ORGANISATIONAL_UNIT.as("ou");
    private final Field<String> replacementAppName = InlineSelectFieldFactory.mkNameField(mrr.ENTITY_ID, mrr.ENTITY_KIND, asSet(EntityKind.APPLICATION));
    private final Field<String> replacementAppExtId = InlineSelectFieldFactory.mkExternalIdField(mrr.ENTITY_ID, mrr.ENTITY_KIND, asSet(EntityKind.APPLICATION));



    @Autowired
    public MeasurableRatingExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {

        String path = WebUtilities.mkPath("data-extract", "measurable-rating", ":id");
        registerRatingsExtract(path);

        String unmappedPath = WebUtilities.mkPath("data-extract", "measurable-rating", "unmapped", ":id");
        registerUnmappedAllocationsExtract(unmappedPath);
    }

    private void registerRatingsExtract(String path) {
        post(path, (request, response) -> {
            long categoryId = WebUtilities.getId(request);
            IdSelectionOptions selectionOpts = WebUtilities.readIdSelectionOptionsFromBody(request);
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
                    .select(DSL
                                    .when(mc.ALLOW_PRIMARY_RATINGS.isTrue().and(mr.IS_PRIMARY.isTrue()), "Y")
                                    .when(mc.ALLOW_PRIMARY_RATINGS.isTrue().and(mr.IS_PRIMARY.isFalse()), "N")
                                    .otherwise("n/a").as("Is Primary"),
                            mr.DESCRIPTION.as("Rating Description"),
                            mr.LAST_UPDATED_AT.as("Last Updated Time"),
                            mr.LAST_UPDATED_BY.as("Last Updated By"))
                    .select(mrd.PLANNED_DECOMMISSION_DATE.as("Planned Decommission Date"))
                    .select(replacementAppName.as("Replacement App Name"),
                            replacementAppExtId.as("Replacement External Id"),
                            mrr.PLANNED_COMMISSION_DATE.as("Replacement Commission Date"))
                    .select(ou.NAME.as("Org Unit Name"));

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
                    .leftJoin(ou).on(ou.ID.eq(app.ORGANISATIONAL_UNIT_ID))
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

    private void registerUnmappedAllocationsExtract(String path) {
        post(path, (request, response) -> {
            long categoryId = WebUtilities.getId(request);

            IdSelectionOptions selectionOpts = WebUtilities.readIdSelectionOptionsFromBody(request);
            Select<Record1<Long>> appIds = applicationIdSelectorFactory.apply(selectionOpts);

            SelectConditionStep<Record1<Long>> appIdsAssignedToAnyMeasurableInTheCategory = dsl
                    .selectDistinct(MEASURABLE_RATING.ENTITY_ID)
                    .from(MEASURABLE_RATING)
                    .join(MEASURABLE)
                    .on(MEASURABLE.ID.eq(MEASURABLE_RATING.MEASURABLE_ID))
                    .where(MEASURABLE_RATING.ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                            .and(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(categoryId)));

            SelectSeekStep1<Record5<String, String, Long, String, String>, String> qry = dsl
                    .selectDistinct(app.NAME.as("App Name"),
                            app.ASSET_CODE.as("App Code"),
                            app.ID.as("App Waltz Id"),
                            app.KIND.as("App Kind"),
                            ou.NAME.as("Org Unit Name"))
                    .from(app)
                    .leftJoin(ou).on(ou.ID.eq(app.ORGANISATIONAL_UNIT_ID))
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
