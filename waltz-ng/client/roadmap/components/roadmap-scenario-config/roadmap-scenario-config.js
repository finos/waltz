

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

import template from "./roadmap-scenario-config.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";


const bindings = {
    scenarioId: "<",
    onCancel: "<",
    onSaveScenarioName: "<",
    onSaveScenarioDescription: "<",
    onSaveScenarioEffectiveDate: "<",
    onSaveScenarioType: "<",
    onAddAxisItem: "<",
    onRemoveAxisItem: "<",
    onRepositionAxisItems: "<"
};


const initialState = {
    scenario: null
};


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    function reload() {
        return serviceBroker
            .loadViewData(
                CORE_API.ScenarioStore.getById,
                [ vm.scenarioId ],
                { force: true })
            .then(r => Object.assign(vm, r.data));
    }


    // -- boot ---

    vm.$onInit = () => {
        reload();
    };


    // -- interact ---

    vm.onSaveName = (data, ctx) => {
        vm.onSaveScenarioName(data, ctx)
            .then(() => reload());
    };

    vm.onSaveDescription = (data, ctx) => {
        vm.onSaveScenarioDescription(data, ctx)
            .then(() => reload());
    };

    vm.onSaveEffectiveDate = (data, ctx) => {
        vm.onSaveScenarioEffectiveDate(data, ctx)
            .then(() => reload());
    };

}


controller.$inject = [
    "$q",
    "ServiceBroker"
];


const component = {
    controller,
    template,
    bindings
};


export default {
    component,
    id: "waltzRoadmapScenarioConfig"
}