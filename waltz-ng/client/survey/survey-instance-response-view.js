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
import {dynamicSections} from "../dynamic-section/dynamic-section-definitions";
import template from "./survey-instance-response-view.html";
import SurveyViewer from "./components/svelte/inline-panel/SurveyViewer.svelte"
import {CORE_API} from "../common/services/core-api-utils";
import _ from "lodash";


const initialState = {
    changeLogSection: dynamicSections.changeLogSection,
    SurveyViewer
};


function controller($stateParams, historyStore, serviceBroker) {

    const vm = initialiseData(this, initialState);
    const id = $stateParams.id;

    vm.entityReference = {
        id,
        kind: "SURVEY_INSTANCE"
    };

    vm.$onInit = () => {
        serviceBroker
            .loadViewData(CORE_API.SurveyInstanceViewStore.getById, [id])
            .then(result => {
                const surveyInstanceInfo = result.data;
                addToHistory(historyStore, surveyInstanceInfo)
            });
    }

}

function determineName(surveyInstance) {
    return _.get(surveyInstance, ["surveyInstance", "surveyEntity", "name"], "Unknown")
        + " - "
        + _.get(surveyInstance, ["surveyRun", "name"], "Survey");
}


const addToHistory = (historyStore, surveyInstance) => {
    if (!surveyInstance) {
        return;
    }
    historyStore.put(
        determineName(surveyInstance),
        "SURVEY_INSTANCE",
        "main.survey.instance.response.view",
        {id: surveyInstance.surveyInstance.id});
};


controller.$inject = [
    "$stateParams",
    "HistoryStore",
    "ServiceBroker"
];


const view = {
    controller,
    controllerAs: "ctrl",
    template
};

export default view;