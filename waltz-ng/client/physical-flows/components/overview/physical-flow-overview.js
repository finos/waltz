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
import template from './physical-flow-overview.html';
import {initialiseData} from "../../../common/index";
import {resolveSourceAndTarget} from "../../../logical-flow/logical-flow-utils";
import {compareCriticalities} from "../../../common/criticality-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import {toEntityRef} from "../../../common/entity-utils";


const bindings = {
    parentEntityRef: '<'
};


const initialState = {
    hasCriticalityMismatch: false
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const load = () => {
        const physicalFlowPromise = serviceBroker
            .loadViewData(
                CORE_API.PhysicalFlowStore.getById,
                [vm.parentEntityRef.id],
                {force: true})
            .then(r => vm.physicalFlow = r.data);

        const logicalFlowPromise = physicalFlowPromise
            .then(physicalFlow => serviceBroker
                .loadViewData(
                    CORE_API.LogicalFlowStore.getById,
                    [vm.physicalFlow.logicalFlowId]))
            .then(r => vm.logicalFlow = r.data);

        physicalFlowPromise
            .then(physicalFlow => serviceBroker
                .loadViewData(
                    CORE_API.PhysicalSpecificationStore.getById,
                    [physicalFlow.specificationId]))
            .then(r => {
                vm.specification = r.data;
                vm.specificationReference = toEntityRef(r.data, 'PHYSICAL_SPECIFICATION');
            });

        logicalFlowPromise.then(() => {
            if (vm.logicalFlow.source.kind === 'APPLICATION') {
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
        })

    };

    vm.$onInit = () => {
        load();
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