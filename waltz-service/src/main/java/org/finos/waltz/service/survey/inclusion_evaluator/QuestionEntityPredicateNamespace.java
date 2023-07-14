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

package org.finos.waltz.service.survey.inclusion_evaluator;

import org.finos.waltz.schema.tables.EntityHierarchy;
import org.finos.waltz.schema.tables.OrganisationalUnit;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.survey.SurveyQuestion;
import org.finos.waltz.model.survey.SurveyQuestionResponse;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.jooq.Table;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.Map;

import static org.finos.waltz.schema.Tables.*;

/**
 *
 * NOTE: methods in this class may show as unused.  This is to be expected as they are referred to via
 * predicates in survey questions
 */
public class QuestionEntityPredicateNamespace extends QuestionBasePredicateNamespace {

    protected final DSLContext dsl;
    protected final EntityReference subjectRef;


    public QuestionEntityPredicateNamespace(DSLContext dsl,
                                            EntityReference subjectRef,
                                            List<SurveyQuestion> questions,
                                            Map<Long, SurveyQuestionResponse> responsesByQuestionId) {
        super(questions, responsesByQuestionId);
        this.dsl = dsl;
        this.subjectRef = subjectRef;
    }


    public String assessmentRating(String name, String defaultVal) {
        return dsl
                .select(RATING_SCHEME_ITEM.CODE)
                .from(ASSESSMENT_DEFINITION)
                .innerJoin(ASSESSMENT_RATING).on(ASSESSMENT_RATING.ASSESSMENT_DEFINITION_ID.eq(ASSESSMENT_DEFINITION.ID))
                .innerJoin(RATING_SCHEME).on(RATING_SCHEME.ID.eq(ASSESSMENT_DEFINITION.RATING_SCHEME_ID))
                .innerJoin(RATING_SCHEME_ITEM).on(RATING_SCHEME_ITEM.ID.eq(ASSESSMENT_RATING.RATING_ID))
                .where(ASSESSMENT_DEFINITION.EXTERNAL_ID.eq(name).or(ASSESSMENT_DEFINITION.NAME.eq(name)))
                .and(ASSESSMENT_RATING.ENTITY_KIND.eq(subjectRef.kind().name()))
                .and(ASSESSMENT_RATING.ENTITY_ID.eq(subjectRef.id()))
                .fetchOptional(RATING_SCHEME_ITEM.CODE)
                .orElse(defaultVal);
    }


    public String assessmentRating(String name) {
        return assessmentRating(name, null);
    }


    public boolean hasInvolvement(String name) {
        return dsl.fetchExists(DSL
                .select()
                .from(INVOLVEMENT)
                .innerJoin(INVOLVEMENT_KIND).on(INVOLVEMENT.KIND_ID.eq(INVOLVEMENT_KIND.ID))
                .where(INVOLVEMENT_KIND.NAME.equalIgnoreCase(name))
                .and(INVOLVEMENT.ENTITY_ID.eq(subjectRef.id()))
                .and(INVOLVEMENT.ENTITY_KIND.eq(subjectRef.kind().name())));
    }


    // --- HELPER ---

    protected boolean belongsToOrgUnit(String name,
                                       Table<?> subjectTable,
                                       Field<Long> subjectId,
                                       Field<Long> subjectOu) {
        EntityHierarchy eh = ENTITY_HIERARCHY.as("eh");
        OrganisationalUnit ou = OrganisationalUnit.ORGANISATIONAL_UNIT.as("ou");

        SelectConditionStep<Record1<Long>> targetOrgUnitId = DSL
                .select(ou.ID)
                .from(ou)
                .where(ou.NAME.eq(name))
                .or(ou.EXTERNAL_ID.eq(name));

        Condition subjectInTargetOuTree = subjectOu.in(DSL
                .selectDistinct(eh.ID)
                .from(eh)
                .where(eh.ANCESTOR_ID.eq(targetOrgUnitId)));

        Condition subjectEntitiesMatch = subjectId.eq(subjectRef.id());

        SelectConditionStep<Record1<Long>> qry = DSL
                .select(subjectId)
                .from(subjectTable)
                .where(subjectEntitiesMatch)
                .and(subjectInTargetOuTree);

        return dsl
                .fetchExists(qry);
    }


    protected boolean hasLifecyclePhase(String name,
                                        Table<?> subjectTable,
                                        Field<Long> subjectId,
                                        Field<String> subjectLifecyclePhase) {


        return dsl
                .fetchExists(DSL
                        .select()
                        .from(subjectTable)
                        .where(subjectLifecyclePhase.eq(name)
                                .and(subjectId.eq(subjectRef.id()))));
    }

}
