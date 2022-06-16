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
import template from "./physical-flow-overview.html";
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import {toEntityRef} from "../../../common/entity-utils";
import {truncate} from "../../../common/string-utils";


const bindings = {
    parentEntityRef: "<"
};


const initialState = {
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
                vm.specificationReference = toEntityRef(r.data, "PHYSICAL_SPECIFICATION");
            });

        serviceBroker
            .loadViewData(
                CORE_API.PhysicalFlowParticipantStore.findByPhysicalFlowId,
                [ vm.parentEntityRef.id ])
            .then(r => {
                vm.participants = _
                    .chain(r.data)
                    .groupBy("kind")
                    .mapValues(ps => _.map(ps, p => {
                        return {
                            id: p.participant.id,
                            kind: p.participant.kind,
                            name: truncate(p.participant.name, 24)
                        };
                    }))
                    .value();
            });
    };


    vm.$onChanges = (change) => {
        if(change.parentEntityRef && vm.parentEntityRef) {
            load();
        }
    };

}


controller.$inject = ["ServiceBroker"];


const component = {
    template,
    controller,
    bindings
};


export default {
    component,
    id: "waltzPhysicalFlowOverview"
};