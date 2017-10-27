/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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
import template from './physical-flow-overview.html';
import {initialiseData} from "../../../common/index";
import {resolveSourceAndTarget} from "../../../logical-flow/logical-flow-utils";
import {compareCriticalities} from "../../../common/criticality-utils";


const bindings = {
    logicalFlow: '<',
    specification: '<',
    physicalFlow: '<'
};


const initialState = {
    hasCriticalityMismatch: false
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        if (vm.logicalFlow && vm.physicalFlow && vm.logicalFlow.source.kind === 'APPLICATION') {
            resolveSourceAndTarget(serviceBroker, vm.logicalFlow)
                .then(sourceAndTarget => {
                    const criticalityComparison = compareCriticalities(
                        sourceAndTarget.source.businessCriticality,
                        vm.physicalFlow.criticality);

                    if (criticalityComparison === -1) {
                        vm.hasCriticalityMismatch = true;
                        vm.criticalityMismatchMessage = "Criticality mismatch: this flow has a higher criticality rating than the source"
                    } else {
                        vm.hasCriticalityMismatch = false;
                    }
                });
        }
    };
}


controller.$inject = ['ServiceBroker'];


const component = {
    template,
    controller,
    bindings
};


export default {
    component,
    id: 'waltzPhysicalFlowOverview'
};