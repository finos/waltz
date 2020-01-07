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
import { CORE_API } from "../../../common/services/core-api-utils";
import template from "./change-initiative-view.html";
import { initialiseData } from "../../../common";


const initialState = {
    changeInitiative: {},
    displayRetiredCis: false,
    related: {
        appGroupRelationships: []
    },
    orgUnit: null,
    entityRef: null,
};


function controller($stateParams,
                    dynamicSectionManager,
                    historyStore,
                    serviceBroker) {

    const {id} = $stateParams;
    const vm = initialiseData(this, initialState);

    vm.entityRef = {
        kind: "CHANGE_INITIATIVE",
        id: id
    };

    vm.$onInit = () => {
        const ciPromise = serviceBroker
            .loadViewData(CORE_API.ChangeInitiativeStore.getById, [id])
            .then(result => {
                vm.changeInitiative = result.data;
                return vm.changeInitiative;
            });

        ciPromise
            .then((ci) => serviceBroker
                .loadViewData(
                    CORE_API.OrgUnitStore.getById,
                    [ ci.organisationalUnitId ])
                .then( r => vm.orgUnit = r.data));

        ciPromise
            .then((ci) => historyStore
                .put(
                    ci.name,
                    "CHANGE_INITIATIVE",
                    "main.change-initiative.view",
                    { id: ci.id }));

        dynamicSectionManager.initialise("CHANGE_INITIATIVE");

    };

    vm.onToggleDisplayRetiredCis = () => {
        vm.displayRetiredCis = ! vm.displayRetiredCis;
    };

}


controller.$inject = [
    "$stateParams",
    "DynamicSectionManager",
    "HistoryStore",
    "ServiceBroker"
];


const page = {
    template,
    controller,
    controllerAs: "$ctrl"
};


export default page;

