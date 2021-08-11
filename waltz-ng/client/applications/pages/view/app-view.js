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

import template from "./app-view.html";


const initialState = {
    app: {},
    parentEntityRef: {}
};


const addToHistory = (historyStore, app) => {
    if (! app) { return; }
    historyStore.put(
        app.name,
        "APPLICATION",
        "main.app.view",
        { id: app.id });
};


function controller($stateParams,
                    serviceBroker,
                    historyStore) {
    const vm = Object.assign(this, initialState);

    function loadAll(id) {
        serviceBroker
            .loadViewData(CORE_API.ApplicationStore.getById, [id])
            .then(r => vm.app = r.data)
            .then(() => postLoadActions(vm.app));
    }


    function postLoadActions(app) {
        addToHistory(historyStore, app);
    }

    // -- BOOT --
    vm.$onInit = () => {
        const id = $stateParams.id;
        const entityReference = { id, kind: "APPLICATION" };
        vm.parentEntityRef = entityReference;
        loadAll(id);
    };

}


controller.$inject = [
    "$stateParams",
    "ServiceBroker",
    "HistoryStore"
];


export default  {
    template,
    controller,
    controllerAs: "ctrl"
};

