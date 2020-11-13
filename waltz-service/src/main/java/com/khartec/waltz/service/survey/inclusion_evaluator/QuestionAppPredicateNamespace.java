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

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.survey.SurveyQuestion;
import com.khartec.waltz.model.survey.SurveyQuestionResponse;
import com.khartec.waltz.schema.tables.Application;
import com.khartec.waltz.schema.tables.DataType;
import com.khartec.waltz.schema.tables.DataTypeUsage;
import com.khartec.waltz.schema.tables.EntityHierarchy;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

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
        Application app = APPLICATION.as("app");
        return belongsToOrgUnit(name, app, app.ID, app.ORGANISATIONAL_UNIT_ID);
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

}
