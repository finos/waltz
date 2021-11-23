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
import {initialiseData} from "../common/index";
import {mkEntityLinkGridCell, mkLinkGridCell} from "../common/grid-utils";
import UserSurveyListPanel from "./components/svelte/UserSurveyListPanel.svelte"
import template from "./survey-instance-list-user-view.html";
import roles from "../user/system-roles";
import {selectSurveyRow} from "./components/svelte/user-survey-store";
import {loadSurveyInfo} from "./survey-utils";
import {questions, responses, surveyDetails} from "./components/svelte/inline-panel/survey-detail-store";
import {CORE_API} from "../common/services/core-api-utils";


const initialState = {
    incompleteColumnDefs: [],
    completeColumnDefs: [],
    surveyInstancesAndRuns: [],
    showSurveyTemplateButton: false,
    UserSurveyListPanel
};

function controller($state,
                    userService) {

    const vm = initialiseData(this, initialState);

    vm.goToSurvey = (surveyInstance) => {
        $state.go(
            "main.survey.instance.response.view",
            { id: surveyInstance.id });
    }

    vm.$onInit = () => {
        selectSurveyRow.set(vm.goToSurvey);

        // loadSurveyInfo($q, serviceBroker, userService, vm.parentEntityRef.id)
        //     .then(details => surveyDetails.set(details));
        //
        // serviceBroker
        //     .loadViewData(
        //         CORE_API.SurveyQuestionStore.findQuestionsForInstance,
        //         [ vm.parentEntityRef.id ])
        //     .then(r => questions.set(r.data));
        //
        // serviceBroker
        //     .loadViewData(
        //         CORE_API.SurveyInstanceStore.findResponses,
        //         [ vm.parentEntityRef.id ])
        //     .then(r => {
        //         return responses.set(r.data);
        //     });
    }

    userService.whoami()
        .then(user => vm.user = user)
        .then(() => vm.showSurveyTemplateButton = userService.hasRole(vm.user, roles.SURVEY_ADMIN)
            || userService.hasRole(vm.user, roles.SURVEY_TEMPLATE_ADMIN));
}


controller.$inject = [
    "$state",
    "UserService"
];


export default {
    template,
    controller,
    controllerAs: "ctrl"
};

