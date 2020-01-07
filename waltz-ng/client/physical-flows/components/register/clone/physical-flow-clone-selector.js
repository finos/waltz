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

import template from './physical-flow-clone-selector.html';

import _ from 'lodash';
import {initialiseData} from "../../../../common/index";
import {CORE_API} from "../../../../common/services/core-api-utils";
import {sameRef} from "../../../../common/entity-utils";
import {combinePhysicalWithLogical} from "../../../physical-flow-utils";


const SEARCH_CUTOFF = 2;


const bindings = {
    parentEntityRef: '<',
    onDismiss: '<',
    onClone: '<'
};


const initialState = {
    inbound: [],
    outbound: [],
    visibility: {
        search: false
    }
};


function sortFlows(fs = []) {
    const fieldOrder = ['logical.target.name', 'specification.name'];
    return _.orderBy(fs, fieldOrder);
}


function controller($q,
                    displayNameService,
                    serviceBroker) {

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const logicalPromise = serviceBroker
            .loadViewData(CORE_API.LogicalFlowStore.findByEntityReference, [ vm.parentEntityRef ])
            .then(r => r.data);

        const physicalPromise = serviceBroker
            .loadViewData(CORE_API.PhysicalFlowStore.findByEntityReference, [ vm.parentEntityRef ])
            .then(r => r.data);

        const specPromise = serviceBroker
            .loadViewData(CORE_API.PhysicalSpecificationStore.findByEntityReference, [ vm.parentEntityRef ])
            .then(r => r.data);

        $q.all([logicalPromise, physicalPromise, specPromise])
            .then(([logicals, physicals, specs]) => {
                const combined = combinePhysicalWithLogical(
                    physicals,
                    logicals,
                    specs,
                    displayNameService);
                const [outbound, inbound] = _.partition(
                    combined,
                    c => sameRef(c.logical.source, vm.parentEntityRef));
                vm.inbound = sortFlows(inbound);
                vm.outbound = sortFlows(outbound);
                vm.visibility.search = vm.outbound.length > SEARCH_CUTOFF;
            });
    };

    vm.onSelect = (flow) => {
        vm.onClone(flow);
    };

}


controller.$inject = [
    '$q',
    'DisplayNameService',
    'ServiceBroker'
];


const component = {
    controller,
    bindings,
    template
};


export default {
    id: 'waltzPhysicalFlowCloneSelector',
    component
}