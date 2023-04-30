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
import template from "./survey-template-create.html";
import toasts from "../svelte-stores/toast-store";
import {displayError} from "../common/error-utils";


const initialState = {
    surveyTemplate: {},
    targetEntityKinds: [{
        name: "Application",
        value: "APPLICATION"
    },{
        name: "Change Initiative",
        value: "CHANGE_INITIATIVE"
    }]
};


function controller($state,
                    surveyTemplateStore) {

    const vm = initialiseData(this, initialState);

    vm.onSubmit = () => {
        surveyTemplateStore
            .create(vm.surveyTemplate)
            .then(templateId => {
                toasts.success("Survey template created successfully");
                $state.go("main.survey.template.edit", {id: templateId});
            }, (e) => {
                displayError("Failed to create survey template", e);
            });
    };
}


controller.$inject = [
    "$state",
    "SurveyTemplateStore"
];


const page = {
    controller,
    controllerAs: "ctrl",
    template
};


export default page;