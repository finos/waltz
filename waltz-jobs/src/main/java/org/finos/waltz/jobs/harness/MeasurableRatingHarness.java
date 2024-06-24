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

package org.finos.waltz.jobs.harness;

import org.finos.waltz.data.measurable_rating.MeasurableRatingIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.ImmutableIdSelectionOptions;
import org.finos.waltz.model.measurable_rating.MeasurableRating;
import org.finos.waltz.model.measurable_rating.MeasurableRatingCategoryView;
import org.finos.waltz.service.DIBaseConfiguration;
import org.finos.waltz.service.measurable.MeasurableService;
import org.finos.waltz.service.measurable_rating.MeasurableRatingService;
import org.finos.waltz.service.measurable_rating.MeasurableRatingViewService;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import java.util.List;

import static org.finos.waltz.common.FunctionUtilities.time;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;


public class MeasurableRatingHarness {

    @Configuration
    @Import(DIBaseConfiguration.class)
    @Lazy
    @ComponentScan("org.finos.waltz.service")
    private interface MyCtx {
    }

    public static void main(String[] args) throws ParseException {

        AnnotationConfigApplicationContext ctx = time("boot", () -> new AnnotationConfigApplicationContext(MyCtx.class));

        MeasurableRatingService measurableRatingSvc = ctx.getBean(MeasurableRatingService.class);
        MeasurableRatingViewService viewSvc = ctx.getBean(MeasurableRatingViewService.class);

        EntityReference ftpPricing = mkRef(EntityKind.MEASURABLE, 73668L);
        EntityReference nH = mkRef(EntityKind.PERSON, 2677360L);

        IdSelectionOptions ftpPricingOptions = mkOpts(ftpPricing);
        IdSelectionOptions nHOptions = mkOpts(nH);

        Select<Record1<Long>> selector = new MeasurableRatingIdSelectorFactory().apply(nHOptions);
        System.out.println("\n\n\n---findForCategoryAndMeasurableRatingIdSelector----------------\n");
        List<MeasurableRating> res = measurableRatingSvc.findForCategoryAndMeasurableRatingIdSelector(selector, 33L);
        res.forEach(r -> System.out.printf("Ent: %d\tMeas: %d\tMR: %d\n", r.entityReference().id(), r.measurableId(), r.id().orElse(-1L)));

        MeasurableRatingCategoryView view = viewSvc.getViewForCategoryAndSelector(nHOptions, 33L);

        System.out.println(view.measurableRatings().measurableRatings().size());

    }

}
