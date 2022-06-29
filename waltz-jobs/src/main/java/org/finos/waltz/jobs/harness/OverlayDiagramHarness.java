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

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.aggregate_overlay_diagram.AggregatedEntitiesWidgetDao;
import org.finos.waltz.data.aggregate_overlay_diagram.AppCountWidgetDao;
import org.finos.waltz.data.aggregate_overlay_diagram.AssessmentRatingWidgetDao;
import org.finos.waltz.model.AssessmentBasedSelectionFilter;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.ImmutableAssessmentBasedSelectionFilter;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.CountWidgetData;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters.ImmutableAppCountWidgetParameters;
import org.finos.waltz.service.DIConfiguration;
import org.finos.waltz.service.aggregate_overlay_diagram.AggregateOverlayDiagramService;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Optional;

import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.finos.waltz.schema.Tables.ASSESSMENT_RATING;


public class OverlayDiagramHarness {

    public static void main(String[] args) throws ParseException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);

        AppCountWidgetDao countWidgetDao = ctx.getBean(AppCountWidgetDao.class);
        AssessmentRatingWidgetDao assessmentWidgetDao = ctx.getBean(AssessmentRatingWidgetDao.class);
        AggregatedEntitiesWidgetDao aggregatedEntitiesWidgetDao = ctx.getBean(AggregatedEntitiesWidgetDao.class);
        AggregateOverlayDiagramService diagramSvc = ctx.getBean(AggregateOverlayDiagramService.class);

        IdSelectionOptions appGroup = mkOpts(mkRef(EntityKind.APP_GROUP, 11785L));
        IdSelectionOptions ou = mkOpts(mkRef(EntityKind.ORG_UNIT, 95L));

        GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();
        GenericSelector genericSelector = genericSelectorFactory.applyForKind(EntityKind.APPLICATION, ou);
        GenericSelector genericSelector2 = genericSelectorFactory.applyForKind(EntityKind.CHANGE_INITIATIVE, ou);

        ImmutableAssessmentBasedSelectionFilter sdlcPassFilter = ImmutableAssessmentBasedSelectionFilter.builder()
                .definitionId(30L)
                .ratingIds(asSet(240L))
                .build();

        ImmutableAssessmentBasedSelectionFilter criticalityFilter = ImmutableAssessmentBasedSelectionFilter.builder()
                .definitionId(73L)
                .ratingIds(asSet(327L))
                .build();


        ImmutableAppCountWidgetParameters countParams = ImmutableAppCountWidgetParameters.builder()
                .targetDate(DateTimeUtilities.today())
                .build();

        CountWidgetData datums = diagramSvc.getAppCountWidgetData(1L, ou, asSet(sdlcPassFilter, criticalityFilter), countParams);

        System.out.println(datums);

        System.out.println("Done");
    }


    private static Select<Record1<Long>> prepareFilteredSelection(EntityKind targetKind,
                                                                  IdSelectionOptions options,
                                                                  Optional<AssessmentBasedSelectionFilter> params) {

        GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();
        GenericSelector genericSelector = genericSelectorFactory.applyForKind(targetKind, options);

        return params
                .map(p -> genericSelector.selector().intersect(mkAssessmentRatingSelector(p, genericSelector.kind())))
                .orElse(genericSelector.selector());
    }


    private static Select<? extends Record1<Long>> mkAssessmentRatingSelector(AssessmentBasedSelectionFilter params,
                                                                              EntityKind targetKind) {
        return DSL
                .select(ASSESSMENT_RATING.ENTITY_ID)
                .from(ASSESSMENT_RATING)
                .where(ASSESSMENT_RATING.ASSESSMENT_DEFINITION_ID.eq(params.definitionId())
                        .and(ASSESSMENT_RATING.RATING_ID.in(params.ratingIds())
                                .and(ASSESSMENT_RATING.ENTITY_KIND.eq(targetKind.name()))));
    }

}
