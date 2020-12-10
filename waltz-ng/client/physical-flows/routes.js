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

const baseState = {
};


import PhysicalFlowView from "./physical-flow-view";
import PhysicalFlowRegistration from "./physical-flow-registration";

const viewState = {
    url: "physical-flow/{id:int}",
    views: {"content@": PhysicalFlowView },
};


const registrationState = {
    url: "physical-flow/registration/{kind:string}/{id:int}?{targetLogicalFlowId:int}",
    views: {"content@": PhysicalFlowRegistration },
};


function setup($stateProvider) {

    $stateProvider
        .state("main.physical-flow", baseState)
        .state("main.physical-flow.registration", registrationState)
        .state("main.physical-flow.view", viewState);
}


setup.$inject = ["$stateProvider"];


export default setup;