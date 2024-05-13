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

import {CORE_API} from "../../../common/services/core-api-utils";
import {initialiseData} from "../../../common";

import template from "./inline-logical-flow-panel.html";
import {entity} from "../../../common/services/enums/entity";
import {flowDirection, flowDirection as FlowDirection} from "../../../common/services/enums/flow-direction";
import FlowClassificationLegend from "../../../flow-classification-rule/components/svelte/FlowClassificationLegend.svelte"


const bindings = {
    parentEntityRef: "<"
};


const initialState = {
    logicalFlows: [],
    logicalFlowDecorators: [],
    physicalFlows: [],
    physicalSpecifications: [],
    ratingDirection: FlowDirection.OUTBOUND.key,
    FlowClassificationLegend
};


function controller(serviceBroker) {

    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        const selector = {
            entityReference: vm.parentEntityRef,
            scope: "EXACT"
        };

        serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.findByEntityReference,
                [vm.parentEntityRef])
            .then(r => vm.logicalFlows = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.DataTypeDecoratorStore.findBySelector,
                [selector, entity.LOGICAL_DATA_FLOW.key])
            .then(r => vm.logicalFlowDecorators = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.PhysicalFlowStore.findByEntityReference,
                [vm.parentEntityRef])
            .then(r => vm.physicalFlows = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.PhysicalSpecificationStore.findByEntityReference,
                [vm.parentEntityRef])
            .then(r => vm.physicalSpecifications = r.data);
    };


    vm.onToggleRatingDirection = () => {
        if(vm.ratingDirection === flowDirection.OUTBOUND.key) {
            vm.ratingDirection = flowDirection.INBOUND.key;
        } else {
            vm.ratingDirection = flowDirection.OUTBOUND.key;
        }
    }
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
    id: "waltzInlineLogicalFlowPanel"
};
