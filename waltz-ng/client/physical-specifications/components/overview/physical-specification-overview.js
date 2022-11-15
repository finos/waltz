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
import template from './physical-specification-overview.html';
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";
import {initialiseData} from "../../../common";


const bindings = {
    specification: '<',
    owningEntity: '<',
    organisationalUnit: '<',
    onSaveFormat: "<",
    onSaveDescription: "<"
};


const initialState = {
    specification: null,
    visibility: {
        overviewEditor: false
    }
};

function controller(serviceBroker) {

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        serviceBroker.loadViewData(CORE_API.PhysicalSpecificationStore.findPermissionsForSpec, [vm.specification.id])
            .then(r => vm.canEdit = _.some(
                r.data,
                d => _.includes(["ADD", "UPDATE", "REMOVE"], d)));
    };

}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    bindings,
    controller,
    template
};

export default component;
