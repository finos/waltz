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

import org.apache.commons.jexl3.*;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.survey.SurveyQuestion;
import org.finos.waltz.model.survey.SurveyQuestionResponse;
import org.jooq.DSLContext;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.finos.waltz.common.MapUtilities.newHashMap;
import static org.finos.waltz.common.StringUtilities.isEmpty;

public class QuestionPredicateEvaluator {

    public static List<SurveyQuestion> eval(DSLContext dsl,
                                     List<SurveyQuestion> qs,
                                     EntityReference subjectRef,
                                     Map<Long, SurveyQuestionResponse> responsesByQuestionId) {

        QuestionBasePredicateNamespace namespace = mkPredicateNameSpace(dsl, qs, subjectRef, responsesByQuestionId);

        JexlBuilder builder = new JexlBuilder();
        JexlEngine jexl = builder
                .namespaces(newHashMap(null, namespace))
                .create();

        namespace.usingEvaluator(jexl);

        List<SurveyQuestion> activeQs = determineActiveQs(qs, jexl);

        return activeQs;
    }




    private static QuestionBasePredicateNamespace mkPredicateNameSpace(DSLContext dsl, List<SurveyQuestion> qs, EntityReference subjectRef, Map<Long, SurveyQuestionResponse> responsesByQuestionId) {
        switch (subjectRef.kind()) {
            case APPLICATION:
                return new QuestionAppPredicateNamespace(
                        dsl,
                        subjectRef,
                        qs,
                        responsesByQuestionId);
            case CHANGE_INITIATIVE:
                return new QuestionChangeInitiativePredicateNamespace(
                        dsl,
                        subjectRef,
                        qs,
                        responsesByQuestionId);
            default:
                return new QuestionBasePredicateNamespace(qs, responsesByQuestionId);
        }
    }


    private static List<SurveyQuestion> determineActiveQs(List<SurveyQuestion> qs, JexlEngine jexl) {
        List<SurveyQuestion> activeQs = qs
                .stream()
                .filter(q -> q
                        .inclusionPredicate()
                        .map(p -> {
                            if (isEmpty(p)) {
                                return true;
                            } else {
                                JexlExpression expr = jexl.createExpression(p);
                                JexlContext jexlCtx = new MapContext();
                                return Boolean.valueOf(expr.evaluate(jexlCtx).toString());
                            }
                        })
                        .orElse(true))
                .collect(Collectors.toList());
        return activeQs;
    }



}
