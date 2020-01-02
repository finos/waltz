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

import {initialiseData} from "../../../common";

import template from "./actor-view.html";
import {CORE_API} from "../../../common/services/core-api-utils";


const initialState = {
    logs: [],
};


function mkHistoryObj(actor) {
    return {
        name: actor.name,
        kind: 'ACTOR',
        state: 'main.actor.view',
        stateParams: { id: actor.id }
    };
}


function addToHistory(historyStore, actor) {
    if (! actor) { return; }

    const historyObj = mkHistoryObj(actor);

    historyStore.put(
        historyObj.name,
        historyObj.kind,
        historyObj.state,
        historyObj.stateParams);
}



function controller($stateParams,
                    dynamicSectionManager,
                    serviceBroker,
                    historyStore) {

    const vm = initialiseData(this, initialState);

    const id = $stateParams.id;
    vm.entityRef = { kind: 'ACTOR', id };

    vm.$onInit = () => {

        dynamicSectionManager.initialise("ACTOR");

        serviceBroker
            .loadViewData(
                CORE_API.ActorStore.getById,
                [ id ])
            .then(r => {
                vm.actor = r.data;
                vm.entityRef = Object.assign({}, vm.entityRef, { name: vm.actor.name });
                addToHistory(historyStore, vm.actor);
            });
    };


}


controller.$inject = [
    '$stateParams',
    'DynamicSectionManager',
    'ServiceBroker',
    'HistoryStore',
];


const view = {
    template,
    controller,
    controllerAs: 'ctrl'
};

export default view;
