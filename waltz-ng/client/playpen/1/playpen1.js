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


import template from "./playpen1.html";
import {initialiseData} from "../../common";
import SurveyViewer from "../../survey/components/svelte/inline-panel/SurveyViewer.svelte";
import {loadSurveyInfo} from "../../survey/survey-utils";
import {questions, responses, surveyDetails} from "./survey-detail-store";
import {CORE_API} from "../../common/services/core-api-utils";

const initData = {
    // parentEntityRef: {id: 20768, kind: "APPLICATION"}
    parentEntityRef: {id: 76823, kind: "SURVEY_INSTANCE"},
    measurableEntityRef: {id: 54566, kind: "MEASURABLE"},
    SurveyViewer
};


function controller($q,
                    serviceBroker,
                    userService) {

    const vm = initialiseData(this, initData);

   vm.$onInit = () => {
       loadSurveyInfo($q, serviceBroker, userService, vm.parentEntityRef.id)
           .then(details => surveyDetails.set(details));

       serviceBroker
           .loadViewData(
               CORE_API.SurveyQuestionStore.findQuestionsForInstance,
               [ vm.parentEntityRef.id ])
           .then(r => questions.set(r.data));

        serviceBroker
           .loadViewData(
               CORE_API.SurveyInstanceStore.findResponses,
               [ vm.parentEntityRef.id ])
           .then(r => {
               return responses.set(r.data);
           });

   }

}

controller.$inject = ["$q", "ServiceBroker", "UserService"];

const view = {
    template,
    controller,
    controllerAs: "$ctrl",
    bindToController: true,
    scope: {}
};

export default view;