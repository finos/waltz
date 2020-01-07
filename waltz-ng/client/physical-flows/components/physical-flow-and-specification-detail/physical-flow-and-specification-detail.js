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

import template from "./physical-flow-and-specification-detail.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {toEntityRef} from "../../../common/entity-utils";


const bindings = {
    physicalFlow: "<",
    specification: "<"
};


const initialState = {
    tags: []
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        if (vm.physicalFlow) {
            serviceBroker
                .loadViewData(
                    CORE_API.TagStore.findTagsByEntityRef,
                    [toEntityRef(vm.physicalFlow)],
                    { force: true })
                .then(r => vm.tags = r.data);
        }
    }
}

controller.$inject = [
    "ServiceBroker"
];

const component = {
    bindings,
    controller,
    template
};


export default {
    id: "waltzPhysicalFlowAndSpecificationDetail",
    component
};