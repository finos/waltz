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

import { CORE_API } from "../common/services/core-api-utils";
import DatabaseView from "./pages/view/database-view";


const baseState = {
    url: "database",
};


const viewState = {
    url: "/{id:int}",
    views: {
        "content@": DatabaseView.id
    }
};


const databaseViewByExternalIdBouncerState = {
    url: "/external-id/{externalId}",
    resolve: { externalIdBouncer }
};


function setup($stateProvider) {
    $stateProvider
        .state("main.database", baseState)
        .state("main.database.view", viewState)
        .state("main.database.external-id", databaseViewByExternalIdBouncerState)
}

setup.$inject = [
    "$stateProvider"
];


export default setup;


function externalIdBouncer($state, $stateParams, serviceBroker) {
    const externalId = $stateParams.externalId;
    serviceBroker
        .loadViewData(
            CORE_API.DatabaseStore.findByExternalId,
            [externalId])
        .then(r => {
            const element = r.data;
            if(element) {
                $state.go("main.database.view", {id: element.id});
            } else {
                console.log(`Cannot find database corresponding to external id: ${externalId}`);
            }
        });
}

externalIdBouncer.$inject = [
    "$state",
    "$stateParams",
    "ServiceBroker"
];
