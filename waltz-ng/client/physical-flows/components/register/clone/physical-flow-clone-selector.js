/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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