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

import LogicalFlowView from "./pages/view/logical-flow-view";
import {CORE_API} from "../common/services/core-api-utils";
import _ from "lodash";
import toasts from "../svelte-stores/toast-store";


const baseState = {
    url: "logical-flow"
};

const viewState = {
    url: "/{id:int}",
    views: {"content@": LogicalFlowView },
};


const logicalFlowViewByExternalIdBouncerState = {
    url: "/external-id/{externalId}",
    resolve: {externalIdBouncer},
};


function setup($stateProvider) {

    $stateProvider
        .state("main.logical-flow", baseState)
        .state("main.logical-flow.view", viewState)
        .state("main.logical-flow.external-id", logicalFlowViewByExternalIdBouncerState)
    ;
}


function externalIdBouncer($state, $stateParams, serviceBroker) {
    const externalId = $stateParams.externalId;
    serviceBroker
        .loadViewData(
            CORE_API.LogicalFlowStore.getByExternalId,
            [externalId])
        .then(r => {
            const flow = r.data;
            if (flow) {
                $state.go("main.logical-flow.view", {id: flow.id});
            } else {
                console.log(`Cannot find logical flow corresponding to external id: ${externalId}`);
                toasts.error(`Cannot find logical flow with the external id: ${externalId}, redirecting to the Waltz home page`);
            }
        });
}


setup.$inject = ["$stateProvider"];
externalIdBouncer.$inject = ["$state", "$stateParams", "ServiceBroker"];


export default setup;