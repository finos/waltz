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

package com.khartec.waltz.jobs.harness;

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.khartec.waltz.model.EntityReference.mkRef;

public class MeasurableRatingExporterHarness {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();

        Select<Record1<Long>> appSelector = applicationIdSelectorFactory.apply(IdSelectionOptions.mkOpts(
                mkRef(EntityKind.ORG_UNIT, 10L),
                HierarchyQueryScope.CHILDREN));
//
//
//        long categoryId = 1L;
//
//        Measurable m = MEASURABLE.as("m");
//        Application app = APPLICATION.as("app");
//        MeasurableCategory mc = MEASURABLE_CATEGORY.as("mc");
//        MeasurableRating mr = MEASURABLE_RATING.as("mr");
//        RatingScheme rs = RATING_SCHEME.as("rs");
//        RatingSchemeItem rsi = RATING_SCHEME_ITEM.as("rsi");
//
//
//        SelectConditionStep<Record> qry = dsl
//                .select(m.NAME.as("Taxonomy Item Name"), m.EXTERNAL_ID.as("Taxonomy Item Code"))
//                .select(app.NAME.as("App Name"), app.ASSET_CODE.as("App Code"), app.ID.as("App Waltz Id"), app.KIND.as("App Kind"))
//                .select(rsi.NAME.as("Rating Name"), rsi.CODE.as("Rating Code"))
//                .select(mr.DESCRIPTION.as("Rating Description"), mr.LAST_UPDATED_AT.as("Last Updated Time"), mr.LAST_UPDATED_BY.as("Last Updated By"))
//                .from(m)
//                .innerJoin(mr).on(mr.MEASURABLE_ID.eq(m.ID))
//                .innerJoin(app).on(app.ID.eq(mr.ENTITY_ID))
//                .innerJoin(mc).on(mc.ID.eq(m.MEASURABLE_CATEGORY_ID))
//                .innerJoin(rs).on(rs.ID.eq(mc.RATING_SCHEME_ID))
//                .innerJoin(rsi).on(rsi.SCHEME_ID.eq(rs.ID))
//                .where(app.ENTITY_LIFECYCLE_STATUS.eq(EntityLifecycleStatus.ACTIVE.name()))
//                .and(m.MEASURABLE_CATEGORY_ID.eq(categoryId))
//                .and(mr.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
//                .and(mr.ENTITY_ID.in(appSelector));
//
//        String csv = qry.fetch().formatCSV();
//        System.out.println(csv);


    }
}
