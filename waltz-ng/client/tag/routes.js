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

import tagApplicationView from "./pages/tag-application-view";
import tagPhysicalFlowView from "./pages/tag-physical-flow-view";
import {CORE_API} from "../common/services/core-api-utils";


const base = {
    url: "tags"
};

const idState = {
    url: "/id/{id:int}"
};

const bouncerState = {
    url: "/overview",
    resolve: {
        bouncer
    }
};

const appViewState = {
    url: "/application",
    views: {"content@": tagApplicationView }
};

const physicalFlowViewState = {
    url: "/physical_flow",
    views: {"content@": tagPhysicalFlowView }
};


function setup($stateProvider) {
    $stateProvider
        .state("main.tag", base)
        .state("main.tag.id", idState)
        .state("main.tag.id.view", bouncerState)
        .state("main.tag.id.application", appViewState)
        .state("main.tag.id.physical_flow", physicalFlowViewState);
}

function bouncer($state, $stateParams, serviceBroker) {
    const id = $stateParams.id;

    serviceBroker
        .loadViewData(
            CORE_API.TagStore.getTagById,
            [ id ])
        .then(r => {
            const tag = r.data;
            $state.go(`main.tag.id.${tag.targetKind.toLowerCase()}`, {id: id}, { location: false });
        });
}

setup.$inject = [
    "$stateProvider"
];

bouncer.$inject = [
    "$state",
    "$stateParams",
    "ServiceBroker"
];


export default setup;