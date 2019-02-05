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

package com.khartec.waltz.jobs.harness;

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;

public class MeasurableRatingExporterHarness {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        ApplicationIdSelectorFactory applicationIdSelectorFactory = ctx.getBean(ApplicationIdSelectorFactory.class);

        Select<Record1<Long>> appSelector = applicationIdSelectorFactory.apply(mkOpts(mkRef(EntityKind.ORG_UNIT, 10L),
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
