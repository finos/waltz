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

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.aggregate_overlay_diagram.AppCountWidgetDao;
import org.finos.waltz.data.aggregate_overlay_diagram.TargetAppCostWidgetDao;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.model.AssessmentBasedSelectionFilter;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.ImmutableAssessmentBasedSelectionFilter;
import org.finos.waltz.service.DIConfiguration;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Optional;

import static java.lang.String.format;
import static org.finos.waltz.common.FunctionUtilities.time;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.finos.waltz.schema.Tables.ASSESSMENT_RATING;
import static org.finos.waltz.schema.Tables.CHANGE_INITIATIVE;


public class OverlayDiagramHarness {

    public static void main(String[] args) throws ParseException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);

        AppCountWidgetDao countWidgetDao = ctx.getBean(AppCountWidgetDao.class);
        TargetAppCostWidgetDao costWidgetDao = ctx.getBean(TargetAppCostWidgetDao.class);

        IdSelectionOptions appGroup = mkOpts(mkRef(EntityKind.APP_GROUP, 11785L));
        IdSelectionOptions ou = mkOpts(mkRef(EntityKind.ORG_UNIT, 95L));

        ApplicationIdSelectorFactory appSelector = new ApplicationIdSelectorFactory();


        AssessmentBasedSelectionFilter assessmentFilter = ImmutableAssessmentBasedSelectionFilter.builder()
                .definitionId(93L)
                .ratingIds(SetUtilities.asSet(381L))
                .build();

        Select<Record1<Long>> selector = prepareFilteredSelection(EntityKind.CHANGE_INITIATIVE, ou, Optional.of(assessmentFilter));
        Select<Record1<Long>> selector2 = prepareFilteredSelection(EntityKind.CHANGE_INITIATIVE, ou, Optional.empty());


        int changeInitiativeCount = dsl.fetchCount(DSL
                .select()
                .from(CHANGE_INITIATIVE)
                .where(CHANGE_INITIATIVE.ID.in(selector)));


        SelectConditionStep<Record> qry = dsl
                .select()
                .from(CHANGE_INITIATIVE)
                .where(CHANGE_INITIATIVE.ID.in(selector));

        SelectConditionStep<Record> qry2 = dsl
                .select()
                .from(CHANGE_INITIATIVE)
                .where(CHANGE_INITIATIVE.ID.in(selector2));

        int changeInitiativeCount2 = dsl.fetchCount(qry2);

        System.out.println(format("with filter: %d", changeInitiativeCount));
        System.out.println(qry);
        System.out.println(changeInitiativeCount2);
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
