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
import SurveyViewer from "./components/svelte/inline-panel/SurveyViewer.svelte"


const initialState = {
    changeLogSection: dynamicSections.changeLogSection,
    SurveyViewer
};


function controller($stateParams) {

    const vm = initialiseData(this, initialState);
    const id = $stateParams.id;

    vm.entityReference = {
        id,
        kind: "SURVEY_INSTANCE"
    };
}


controller.$inject = [
    "$stateParams",
];


const view = {
    controller,
    controllerAs: "ctrl",
    template
};

export default view;