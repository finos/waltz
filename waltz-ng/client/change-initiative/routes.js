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

import ChangeInitiativeView from "./pages/view/change-initiative-view";
import {CORE_API} from "../common/services/core-api-utils";
import ChangeInitiativeExternalIdView from "./pages/external-id-view/change-initiative-external-id-view";

const baseState = {
    url: "change-initiative"
};


const viewState = {
    url: "/{id:int}",
    views: { "content@": ChangeInitiativeView }
};


const viewByExternalIdState = {
    url: "/external-id/{externalId}",
    views: {
        "content@": ChangeInitiativeExternalIdView
    },
    resolve: { changeInitiatives: changeInitiativeResolver }
};



function changeInitiativeResolver(serviceBroker, $stateParams) {
    return serviceBroker
        .loadViewData(CORE_API.ChangeInitiativeStore.findByExternalId, [ $stateParams.externalId ])
        .then(r => r.data);
}

changeInitiativeResolver.$inject = ["ServiceBroker", "$stateParams"];




function setupRoutes($stateProvider) {
    $stateProvider
        .state("main.change-initiative", baseState)
        .state("main.change-initiative.view", viewState)
        .state("main.change-initiative.external-id", viewByExternalIdState);
}


setupRoutes.$inject = ["$stateProvider"];


export default setupRoutes;