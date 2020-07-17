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

import _ from "lodash";
import BigEval from "bigeval";


export function groupQuestions(questionInfos = []) {
    const sections = _
        .chain(questionInfos)
        .map(q => q.question.sectionName || "Other")
        .uniq()
        .value();

    const groupedQuestionInfos = _.groupBy(questionInfos, q => q.question.sectionName || "Other");

    return _.map(sections, s => {
        return {
            "sectionName": s,
            "questionInfos": groupedQuestionInfos[s]
        };
    });
}


export function isSurveyTargetKind(entityKind = "") {
    return entityKind === "APPLICATION"
            || entityKind === "CHANGE_INITIATIVE";
}



export function mkDescription(descriptions = []) {
    return _
        .chain(descriptions)
        .filter(d => !_.isEmpty(d))
        .uniq()
        .join("\n\n --- \n\n")
        .value();
}



function lookupQuestion(questions = [],
                        qExtId,
                        expr = "") {
    const referencedQuestion = _.find(questions, d => d.question.externalId === qExtId);
    if (!referencedQuestion) {
        console.log(`SurveyVisibilityCondition [${expr}]: Cannot find referenced question with external id: ${qExtId}`);
    }
    return referencedQuestion;
}


function lookupResponse(responses, q) {
    const referencedId = q.question.id;
    return _.find(responses,r => r.questionId === referencedId);
}


function lookupQuestionResponse(questions = [], responses = [], questionExternalId, expr = "") {
    const question = lookupQuestion(questions, questionExternalId, expr);
    const response = question
        ? lookupResponse(responses, question)
        : null;
    return [question, response]
}


export function mkSurveyExpressionEvaluator(questions = [], responses = []) {
    const ctx = {
        isChecked: (qExtId, dfltVal = false) => {
            const [q,r] = lookupQuestionResponse(questions, responses, qExtId, "isChecked");
            return r
                ? JSON.parse(r.booleanResponse.toLowerCase())
                : dfltVal;
        },
        resp: (qExtId) => {
            const [q,r] = lookupQuestionResponse(questions, responses, qExtId, "resp");
            return r;
        }
    };
    return new BigEval(ctx);
}

