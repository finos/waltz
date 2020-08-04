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
import {initialiseData} from "../../../common/index";


import template from "./historical-responses-panel.html";
import {CORE_API} from "../../../common/services/core-api-utils";
import * as _ from "lodash";


const bindings = {
    parentEntityRef: "<",
    question: "<",
    onSelect: "<",
    onCancel: "<"
};


const initialState = {
    historicalResponses: [],
    selectedResponse: null
};


function controller($q, serviceBroker) {

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        serviceBroker
            .loadViewData(CORE_API.SurveyInstanceStore.findHistoricalResponses,
                [vm.parentEntityRef.id, vm.question.question.id])
            .then(r =>  vm.historicalResponses = _.chain(r.data)
                    .map(d => Object.assign({}, d, {response: vm.getResponse(d)}))
                    .orderBy('lastUpdatedAt', 'desc')
                    .uniqBy(d => d.response)
                    .value());
    };

    vm.onSelectResponse = (response) => {
        vm.selectedResponse = response;
        vm.onSelect(response);
    };

    vm.isSelected = (response) => {
        return vm.selectedResponse === response;
    };

    vm.clearSelection = () => {
        vm.selectedResponse = null;
        vm.onCancel();
    };

    vm.getResponse = (response) => {

        const questionResponse = response.questionResponse;

        switch (vm.question.question.fieldType){
            case 'BOOLEAN':
                return _.get(questionResponse, 'booleanResponse', null);
            case 'NUMBER':
                return _.get(questionResponse, 'numberResponse', null);
            case 'TEXT':
            case 'TEXTAREA':
            case 'DROPDOWN':
                return _.get(questionResponse, 'stringResponse', null);
            case 'DATE':
                return _.get(questionResponse, 'dateResponse', null);
            case 'DROPDOWN_MULTI_SELECT':
                return _.join(_.get(questionResponse, 'listResponse', null), ', ');
            default:
                return null;
        }
    }
}


controller.$inject = [
    "$q",
    "ServiceBroker"
];

const component = {
    bindings,
    template,
    controller
};

export default {
    component,
    id: "waltzHistoricalResponsesPanel"
};



