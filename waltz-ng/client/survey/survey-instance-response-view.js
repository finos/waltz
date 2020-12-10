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

import {initialiseData} from "../common";
import * as SurveyUtils from "./survey-utils";
import {dynamicSections} from "../dynamic-section/dynamic-section-definitions";
import template from "./survey-instance-response-view.html";
import {CORE_API} from "../common/services/core-api-utils";


const initialState = {
    changeLogSection: dynamicSections.changeLogSection
};


function controller($q,
                    $stateParams,
                    serviceBroker,
                    userService) {

    const vm = initialiseData(this, initialState);
    const id = $stateParams.id;

    vm.entityReference = {
        id,
        kind: "SURVEY_INSTANCE"
    };

    SurveyUtils
        .loadSurveyInfo($q, serviceBroker, userService, id)
        .then(details => vm.surveyDetails = details);

    const questionPromise = serviceBroker
        .loadViewData(
            CORE_API.SurveyQuestionStore.findForInstance,
            [ id ])
        .then(r => r.data);

    const responsePromise= serviceBroker
        .loadViewData(
            CORE_API.SurveyInstanceStore.findResponses,
            [ id ])
        .then(r => {
            return r.data;
        });

    $q.all([questionPromise, responsePromise])
        .then(([allQuestions, surveyResponses]) => {
            vm.answersById = SurveyUtils.indexResponses(surveyResponses);
            vm.groupedQuestions = SurveyUtils.groupQuestions(allQuestions);
        });

}


controller.$inject = [
    "$q",
    "$stateParams",
    "ServiceBroker",
    "UserService"
];


const view = {
    controller,
    controllerAs: "ctrl",
    template
};

export default view;