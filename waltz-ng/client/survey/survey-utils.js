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


/**
 * Using a list of questions and responses will attempt to return a tuple array
 * with the `[question?, response?]` which matches a given question external id.
 *
 * @param questions
 * @param responses
 * @param questionExternalId
 * @param exprStr  message used in logging message to help diagnose which function caused this error
 * @returns {*[]}
 */
function lookupQuestionResponse(questions = [],
                                responses = [],
                                questionExternalId,
                                exprStr = "") {
    const question = lookupQuestion(questions, questionExternalId, exprStr);
    const response = question
        ? lookupResponse(responses, question)
        : null;
    return [question, response]
}


/**
 * Given a set of questions and the current state of any responses this
 * will return an object which can evaluate expressions.
 *
 * Currently supported operations are:
 * ```
 *  ['!'],  // Factorial
 *  ['**'],  // power
 *  ['/', '*', '%'],
 *  ['+', '-'],
 *  ['<<', '>>'],  // bit shifts
 *  ['<', '<=', '>', '>='],
 *  ['==', '=', '!='],   // equality comparisons
 *  ['&'], ['^'], ['|'],   // bitwise operations
 *  ['&&'], ['||']   // logical operations
 * ```
 *
 * The evaluator also has built in functions:
 *
 * - `isChecked(questionExtId, defaultValue = false)` : returns boolean value of
 *    a response (or default if response is not given)
 *
 *  For more information see [BigEval](https://github.com/aviaryan/BigEval.js)
 *
 * @param questions
 * @param responses
 * @returns {BigEval}
 */
export function mkSurveyExpressionEvaluator(questions = [], responses = []) {
    let ctx = {};
    const evaluator = new BigEval(ctx);
    Object.assign(ctx, {
        isChecked: (qExtId, dfltVal = false) => {
            const [q,r] = lookupQuestionResponse(questions, responses, qExtId, "isChecked");
            return r
                ? JSON.parse(r.booleanResponse.toLowerCase())
                : dfltVal;
        },
        numberValue: (qExtId, dfltVal = 0) => {
            const [q, r] = lookupQuestionResponse(questions, responses, qExtId, "numberValue");
            return r
                ? r.numberResponse
                : dfltVal;
        },
        ditto: (qExtId) => {
            const q = lookupQuestion(questions, qExtId, "ditto");
            if (q) {
                return evaluator.exec(q.question.inclusionPredicate);
            }
            return true
        },
        resp: (qExtId) => {
            const [q,r] = lookupQuestionResponse(questions, responses, qExtId, "resp");
            return r;
        }
    });
    return evaluator;
}

