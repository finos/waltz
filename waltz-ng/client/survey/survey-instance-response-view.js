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
import {initialiseData} from "../common";
import {groupQuestions} from "./survey-utils";
import {dynamicSections} from "../dynamic-section/dynamic-section-definitions";
import template from "./survey-instance-response-view.html";
import {CORE_API} from "../common/services/core-api-utils";


const initialState = {
    changeLogSection: dynamicSections.changeLogSection
};


function extractAnswer(response = {}) {
    return !_.isNil(response.booleanResponse)
            ? response.booleanResponse
            : (response.stringResponse
                || response.numberResponse
                || response.dateResponse
                || response.entityResponse
                || response.listResponse)
}


function indexResponses(rs = []) {
    return _.chain(rs)
        .map("questionResponse")
        .map(qr => ({
            questionId: qr.questionId,
            answer: extractAnswer(qr),
            comment: qr.comment
        }))
        .keyBy("questionId")
        .value();
}


function controller($stateParams,
                    serviceBroker) {

    const vm = initialiseData(this, initialState);
    const id = $stateParams.id;

    vm.entityReference = {
        id,
        kind: "SURVEY_INSTANCE"
    };

    serviceBroker
        .loadViewData(
            CORE_API.SurveyInstanceStore.getById,
            [ id ])
        .then(r => {
            vm.surveyInstance = r.data;
            return serviceBroker
                .loadViewData(
                    CORE_API.SurveyRunStore.getById,
                    [ vm.surveyInstance.surveyRunId ]);
        })
        .then(r => vm.surveyRun = r.data);

    serviceBroker
        .loadViewData(
            CORE_API.SurveyQuestionStore.findForInstance,
            [ id ])
        .then(r => vm.surveyQuestionInfos = groupQuestions(r.data));

    serviceBroker
        .loadViewData(
            CORE_API.SurveyInstanceStore.findResponses,
            [ id ])
        .then(r => {
            vm.answers = indexResponses(r.data);
        });

}


controller.$inject = [
    "$stateParams",
    "ServiceBroker"
];


const view = {
    controller,
    controllerAs: "ctrl",
    template
};

export default view;