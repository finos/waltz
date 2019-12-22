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

import { initialiseData } from "../../../common";
import { CORE_API } from '../../../common/services/core-api-utils';
import { dynamicSections } from "../../../dynamic-section/dynamic-section-definitions";

import template from "./server-view.html";


const bindings = {
};


const initialState = {
    serverInfo: null,

    appsSection: dynamicSections.appsSection,
    bookmarksSection: dynamicSections.bookmarksSection,
    entityNamedNotesSection: dynamicSections.entityNamedNotesSection,
    changeLogSection: dynamicSections.changeLogSection
};


const addToHistory = (historyStore, server) => {
    if (! server) { return; }
    historyStore.put(
        server.hostname,
        'SERVER',
        'main.server.view',
        { id: server.id });
};


function controller($stateParams, historyStore, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        vm.serverId = $stateParams.id;
        vm.parentEntityRef = {
            kind: "SERVER",
            id: vm.serverId
        };

        serviceBroker
            .loadViewData(CORE_API.ServerInfoStore.getById, [vm.parentEntityRef.id])
            .then(r => {
                vm.serverInfo = r.data;
                addToHistory(historyStore, vm.serverInfo);
            });
    };
}


controller.$inject = [
    "$stateParams",
    "HistoryStore",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzServerView"
};
