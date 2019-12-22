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

import {CORE_API} from "../../../common/services/core-api-utils";

import template from "./app-group-view.html";


const initialState = {
    filters: {},
    groupDetail: null,
};


function controller($stateParams,
                    dynamicSectionManager,
                    serviceBroker,
                    historyStore) {

    const { id }  = $stateParams;

    const vm = Object.assign(this, initialState);

    vm.entityRef = {
        id,
        kind: 'APP_GROUP'
    };

    // -- BOOT --
    vm.$onInit = () => {

        dynamicSectionManager.initialise("APP_GROUP");

        serviceBroker
            .loadViewData(CORE_API.AppGroupStore.getById, [id])
            .then(r => {
                vm.groupDetail = r.data;
                vm.entityRef = Object.assign({}, vm.entityRef, {name: vm.groupDetail.appGroup.name});
                historyStore.put(
                    vm.groupDetail.appGroup.name,
                    'APP_GROUP',
                    'main.app-group.view',
                    { id });
            });

    };

    vm.filtersChanged = (filters) => {
        vm.filters = filters;
    };


    // -- INTERACT ---

    vm.isGroupEditable = () => {
        if (!vm.groupDetail) return false;
        if (!vm.user) return false;
        return _.some(vm.groupDetail.members, isUserAnOwner );
    };

    // -- HELPER ---

    const isUserAnOwner = member =>
        member.role === 'OWNER'
        && member.userId === vm.user.userName;
}


controller.$inject = [
    '$stateParams',
    'DynamicSectionManager',
    'ServiceBroker',
    'HistoryStore'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};
