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
import {initialiseData} from "../common/index";
import UserSurveyListPanel from "./components/svelte/UserSurveyListPanel.svelte"
import template from "./survey-instance-list-user-view.html";
import roles from "../user/system-roles";


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

