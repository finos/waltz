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

import {initialiseData} from "../../common/index";
import template from "./playpen3.html";
import {CORE_API} from "../../common/services/core-api-utils";
import _ from "lodash";


const initialState = {
    parentEntityRef: {
        id: 45832,
        kind: "SURVEY_INSTANCE"
    },
};

function controller($stateParams, serviceBroker) {
    const vm = initialiseData(this, initialState);

    serviceBroker
        .loadViewData(CORE_API.SurveyQuestionStore.findForInstance, [45832])
        .then(r =>  {
            console.log(r.data);
            return vm.selectedQuestion = _.find(r.data, d => d.question.id === 1164)
        });

    vm.onSelectHistoricalResponse = () =>  {
        console.log("Selecting")
    };

    vm.clearSelected = () => {
        console.log("Clear selected response")
    }
}


controller.$inject = [
    "$stateParams",
    "ServiceBroker"
];


const view = {
    template,
    controller,
    controllerAs: "$ctrl",
    bindToController: true,
    scope: {}
};


export default view;
