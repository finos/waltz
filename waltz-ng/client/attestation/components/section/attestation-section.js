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

import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";

const initialState = {
    attestationInstancesAndRuns: []
};

const bindings = {
    parentEntityRef: '<'
};


import template from './attestation-section.html';


function mkAttestationData(attestationRuns = [], attestationInstances = []){
    const runsById = _.keyBy(attestationRuns, 'id');

    return _.map(attestationInstances, instance => ({
        'instance': instance,
        'run': runsById[instance.attestationRunId]
    }));
}



function controller($q,
                    serviceBroker) {

    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        if (vm.parentEntityRef) {
            const runsPromise = serviceBroker
                .loadViewData(CORE_API.AttestationRunStore.findByEntityRef, [vm.parentEntityRef])
                .then(r => r.data);

            const instancesPromise = serviceBroker
                .loadViewData(CORE_API.AttestationInstanceStore.findByEntityRef, [vm.parentEntityRef])
                .then(r => r.data);

            $q.all([runsPromise, instancesPromise])
                .then(([runs, instances]) => vm.attestations = mkAttestationData(runs, instances));
        }
    };
}


controller.$inject = [
    '$q',
    'ServiceBroker'
];


const component = {
    bindings,
    template,
    controller
};


export default {
    component,
    id: 'waltzAttestationSection'
};


