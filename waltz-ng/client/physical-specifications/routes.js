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
import View from "./physical-specification-view";
import {CORE_API} from "../common/services/core-api-utils";
import _ from "lodash";
import toasts from "../svelte-stores/toast-store";


const baseState = {
    url: "physical-specification"
};


const viewState = {
    url: "/{id:int}",
    views: {"content@": View },
};


const physicalSpecViewByExternalIdBouncerState = {
    url: "/external-id/{externalId}",
    resolve: {externalIdBouncer},
};


function setup($stateProvider) {

    $stateProvider
        .state("main.physical-specification", baseState)
        .state("main.physical-specification.view", viewState)
        .state("main.physical-specification.external-id", physicalSpecViewByExternalIdBouncerState);
}



function externalIdBouncer($state, $stateParams, serviceBroker) {
    const externalId = $stateParams.externalId;
    serviceBroker
        .loadViewData(
            CORE_API.PhysicalSpecificationStore.findByExternalId,
            [externalId])
        .then(r => {
            const specList = r.data;
            const spec = _.head(specList);

            if (_.size(specList) > 1 && spec) {
                console.log(`Multiple physical specifications corresponding to external id: ${externalId}`);
                toasts.warning(`Multiple physical flows specifications to external id: ${externalId}, redirecting to the first found`);
                $state.go("main.physical-specification.view", {id: spec.id});
            } else if(_.size(specList) === 1 && spec){
                $state.go("main.physical-specification.view", {id: spec.id});
            } else {
                console.log(`Cannot find physical specification corresponding to external id: ${externalId}`);
                toasts.error(`Cannot find physical specification with the external id: ${externalId}, redirecting to the Waltz home page`);
            }
        });
}

setup.$inject = ["$stateProvider"];

externalIdBouncer.$inject = ["$state", "$stateParams", "ServiceBroker"];

export default setup;