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

package com.khartec.waltz.service.survey.inclusion_evaluator;

import com.khartec.waltz.common.LoggingUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.survey.SurveyQuestion;
import com.khartec.waltz.model.survey.SurveyQuestionResponse;
import com.khartec.waltz.schema.tables.*;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;

/**
 *
 * NOTE: methods in this class may show as unused.  This is to be expected as they are referred to via
 * predicates in survey questions
 */
public class QuestionAppPredicateNamespace extends QuestionEntityPredicateNamespace {

    public QuestionAppPredicateNamespace(DSLContext dsl,
                                         EntityReference subjectRef,
                                         List<SurveyQuestion> questions,
                                         Map<Long, SurveyQuestionResponse> responsesByQuestionId) {
        super(dsl, subjectRef, questions, responsesByQuestionId);
    }


    public boolean isRetiring() {
        Condition isPlanned = APPLICATION.PLANNED_RETIREMENT_DATE.isNotNull();

        Condition notRetiredYet = APPLICATION.ACTUAL_RETIREMENT_DATE.isNull()
                .or(APPLICATION.ACTUAL_RETIREMENT_DATE.greaterOrEqual(DSL.now()));

        return dsl
                .select(APPLICATION.PLANNED_RETIREMENT_DATE)
                .from(APPLICATION)
                .where(APPLICATION.ID.eq(subjectRef.id()))
                .and(isPlanned)
                .and(notRetiredYet)
                .fetch()
                .isNotEmpty();
    }


    public boolean belongsToOrgUnit(String name) {
        EntityHierarchy eh = ENTITY_HIERARCHY.as("eh");
        Application app = APPLICATION.as("app");
        OrganisationalUnit ou = OrganisationalUnit.ORGANISATIONAL_UNIT.as("ou");

        SelectConditionStep<Record1<Long>> qryGivenOrgUnit = DSL
                .select(ou.ID)
                .from(ou)
                .where(ou.NAME.eq(name))
                .or(ou.EXTERNAL_ID.eq(name));

        Condition appInOrgUnitTree = app.ORGANISATIONAL_UNIT_ID.in(DSL
                .selectDistinct(eh.ID)
                .from(eh)
                .where(eh.ANCESTOR_ID.eq(qryGivenOrgUnit)));

        Condition appMatchesSubject = app.ID.eq(subjectRef.id());

        return dsl.fetchExists(DSL
                .select(app.ID)
                .from(app)
                .where(appMatchesSubject)
                .and(appInOrgUnitTree));
    }


    public boolean hasDataType(String name){
        return ! dataTypeUsages(name).isEmpty();
    }


    public Set<String> dataTypeUsages(String name){

        DataTypeUsage dtu = DATA_TYPE_USAGE.as("dtu");
        DataType dt = DATA_TYPE.as("dt");
        EntityHierarchy eh = ENTITY_HIERARCHY.as("eh");

        Condition dtNameMatches = dt.CODE.eq(name)
                .or(dt.NAME.eq(name));

        Condition subjectMatches = dtu.ENTITY_ID.eq(subjectRef.id())
                .and(dtu.ENTITY_KIND.eq(subjectRef.kind().name()));

        return dsl
                .select(dtu.USAGE_KIND)
                .from(dt)
                .innerJoin(eh)
                .on(eh.ANCESTOR_ID.eq(dt.ID).and(eh.KIND.eq(EntityKind.DATA_TYPE.name())))
                .innerJoin(dtu)
                .on(dtu.DATA_TYPE_ID.eq(eh.ID))
                .where(dtNameMatches)
                .and(subjectMatches)
                .fetchSet(dtu.USAGE_KIND);
    }

    public String getRating(String category, String nameOrExtId) {

        MeasurableRating mr = MEASURABLE_RATING.as("mr");
        MeasurableCategory mc = MEASURABLE_CATEGORY.as("mc");
        Measurable m = MEASURABLE.as("m");
        EntityHierarchy eh = ENTITY_HIERARCHY.as("eh");

        Condition categoryMatches = mc.NAME.equalIgnoreCase(category)
                .or(mc.EXTERNAL_ID.equalIgnoreCase(category));

        SelectConditionStep<Record1<Long>> referencedMeasurableId = DSL
                .select(m.ID)
                .from(m)
                .innerJoin(mc).on(m.MEASURABLE_CATEGORY_ID.eq(mc.ID).and(categoryMatches))
                .where(m.EXTERNAL_ID.equalIgnoreCase(nameOrExtId))
                .or(m.NAME.equalIgnoreCase(nameOrExtId));

        SelectConditionStep<Record1<Long>> relatedMeasurables = DSL
                .select(eh.ID)
                .from(eh)
                .where(eh.ANCESTOR_ID.eq(referencedMeasurableId))
                .and(eh.KIND.eq(EntityKind.MEASURABLE.name()));

        List<String> res = dsl
                .select(mr.RATING)
                .from(mr)
                .where(mr.MEASURABLE_ID.in(relatedMeasurables))
                .and(mr.ENTITY_ID.eq(subjectRef.id()))
                .and(mr.ENTITY_KIND.eq(subjectRef.kind().name()))
                .fetch(mr.RATING);

        System.out.println("Res: " + res);

        return "";

    }


    public static void main(String[] args) {
        LoggingUtilities.configureLogging();
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        QuestionAppPredicateNamespace ns = new QuestionAppPredicateNamespace(
                ctx.getBean(DSLContext.class),
                EntityReference.mkRef(EntityKind.APPLICATION, 239L),
                Collections.emptyList(),
                Collections.emptyMap());

        System.out.println("-------------");
        ns.getRating("CAPABILITY", "Dispute Management");
        System.out.println("-------------");
    }

}
