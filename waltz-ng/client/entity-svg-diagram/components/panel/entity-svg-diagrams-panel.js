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

import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import template from "./entity-svg-diagrams-panel.html";


const bindings = {
    parentEntityRef: "<"
};


const initialState = {
    diagrams: [],
    visibility: {}
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        serviceBroker
            .loadViewData(CORE_API.EntitySvgDiagramStore.findByEntityReference, [vm.parentEntityRef])
            .then(result => {
                vm.diagrams = result.data;
                vm.visibility.tab = vm.diagrams.length > 0 ? vm.diagrams[0].id : null;
            });
    };

}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzEntitySvgDiagramsPanel"
};
