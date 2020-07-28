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
import moment from "moment";
import {formats} from "../common";


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


function lookupResponse(responsesById, q) {
    const referencedId = q.question.id;
    return responsesById[q.question.id];
}


/**
 * Using a list of questions and responses will attempt to return a tuple array
 * with the `[question?, response?]` which matches a given question external id.
 *
 * @param questions
 * @param responsesById
 * @param questionExternalId
 * @param exprStr  message used in logging message to help diagnose which function caused this error
 * @returns {*[]}
 */
function lookupQuestionResponse(questions = [],
                                responsesById = {},
                                questionExternalId,
                                exprStr = "") {
    const question = lookupQuestion(questions, questionExternalId, exprStr);
    const response = question
        ? lookupResponse(responsesById, question)
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
 *  Implementation is provided by [BigEval](https://github.com/aviaryan/BigEval.js)
 *
 * @param questions
 * @param responsesById
 * @returns function which takes an expression and gives a result
 */
export function mkSurveyExpressionEvaluator(questions = [], responsesById = {}) {
    let ctx = {};
    const evaluator = new BigEval(ctx);
    Object.assign(ctx, {
        isChecked: (qExtId, dfltVal = false) => {
            const [q,r] = lookupQuestionResponse(questions, responsesById, qExtId, "isChecked");
            return r
                ? JSON.parse(r.booleanResponse.toLowerCase())
                : dfltVal;
        },
        numberValue: (qExtId, dfltVal = 0) => {
            const [q, r] = lookupQuestionResponse(questions, responsesById, qExtId, "numberValue");
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
            const [q,r] = lookupQuestionResponse(questions, responsesById, qExtId, "resp");
            return r;
        }
    });
    return (expr, message, ctx) => {
        const result = evaluator.exec(expr);
        // leaving this here because it's very useful!
        // console.log({expr, message, ctx, result})
        return result;
    }
}


/**
 * Question should be included if it's inclusion predicate evaulates to `true` or it
 * does not have an incusion predicate
 * @param q
 * @param evaluator
 * @returns boolean
 */
function shouldQuestionBeIncluded(q, evaluator) {
    return q.question.inclusionPredicate
        ? evaluator(q.question.inclusionPredicate, q.question.externalId, q)
        : true;
}


/**
 * Give a list of all questions and indexed responses returns a grouped
 * collection of included questions.  Inclusion is determined by
 * evaluating any inclusionPredicate against the current responses.
 *
 * @param allQuestions
 * @param responsesById
 * @returns grouped questions
 */
export function refreshQuestions(allQuestions = [], responsesById = {}) {
    const inclusionEvaluator = mkSurveyExpressionEvaluator(allQuestions, responsesById);
    const activeQs = _.filter(allQuestions, q => shouldQuestionBeIncluded(q, inclusionEvaluator));
    return groupQuestions(activeQs);
}


export function indexResponses(responses = []) {
    return _
        .chain(responses)
        .map(d => d.questionResponse)
        .map(qr => {
            if (!_.isNil(qr.booleanResponse) && !_.isString(qr.booleanResponse)) {
                qr.booleanResponse = qr.booleanResponse
                    ? "true"
                    : "false";
            }
            if (_.isNil(qr.booleanResponse) && !_.isString(qr.booleanResponse)){
                qr.booleanResponse = "null"
            }
            if (!_.isNil(qr.dateResponse)) {
                qr.dateResponse = moment(qr.dateResponse, formats.parseDateOnly).toDate()
            }
            return qr;
        })
        .keyBy("questionId")
        .value();
}

