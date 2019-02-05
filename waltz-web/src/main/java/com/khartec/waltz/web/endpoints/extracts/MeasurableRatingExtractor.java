/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
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
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.schema.Tables;
import com.khartec.waltz.schema.tables.*;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.schema.tables.Measurable.MEASURABLE;
import static com.khartec.waltz.schema.tables.MeasurableRating.MEASURABLE_RATING;
import static com.khartec.waltz.web.WebUtilities.*;
import static org.jooq.tools.StringUtils.toCamelCase;
import static spark.Spark.post;


@Service
public class MeasurableRatingExtractor extends BaseDataExtractor {

    private final ApplicationIdSelectorFactory applicationIdSelectorFactory;
    private final Measurable m = MEASURABLE.as("m");
    private final Application app = APPLICATION.as("app");
    private final MeasurableCategory mc = Tables.MEASURABLE_CATEGORY.as("mc");
    private final MeasurableRating mr = MEASURABLE_RATING.as("mr");
    private final RatingScheme rs = RATING_SCHEME.as("rs");
    private final RatingSchemeItem rsi = RATING_SCHEME_ITEM.as("rsi");


    @Autowired
    public MeasurableRatingExtractor(DSLContext dsl, ApplicationIdSelectorFactory applicationIdSelectorFactory) {
        super(dsl);
        this.applicationIdSelectorFactory = applicationIdSelectorFactory;
    }


    @Override
    public void register() {

        String path = mkPath("data-extract", "measurable-rating", ":id");
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
                            mr.LAST_UPDATED_BY.as("Last Updated By"));

            Condition reportConditions = app.ENTITY_LIFECYCLE_STATUS.eq(EntityLifecycleStatus.ACTIVE.name())
                    .and(m.MEASURABLE_CATEGORY_ID.eq(categoryId))
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
                    .where(reportConditions);

            String csv = qry.fetch().formatCSV();

            String categoryName = dsl.select(mc.NAME).from(mc).where(mc.ID.eq(categoryId)).fetchOne(mc.NAME);
            String suggestedFilename = toCamelCase(categoryName)+".csv";

            return writeFile(
                    suggestedFilename,
                    csv,
                    response);
        });
    }

}
