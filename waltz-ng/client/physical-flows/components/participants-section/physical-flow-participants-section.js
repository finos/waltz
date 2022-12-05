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

import _ from "lodash";
import { CORE_API } from "../../../common/services/core-api-utils";
import { initialiseData } from "../../../common";

import template from "./physical-flow-participants-section.html";
import {displayError} from "../../../common/error-utils";
import toasts from "../../../svelte-stores/toast-store";


const bindings = {
    parentEntityRef: "<",
    onInitialise: "<"
};


const initialState = {
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const onAddParticipant = (s, kind) => {
        return serviceBroker
            .execute(
                CORE_API.PhysicalFlowParticipantStore.add,
                [ vm.physicalFlow.id, kind, { kind: "SERVER", id: s.id } ])
            .then(() => {
                toasts.success("Participant added");
                reloadParticipants();
            })
            .catch(e => displayError("Failed to add participant" , e));
    };

    function reloadParticipants() {
        serviceBroker
            .loadViewData(
                CORE_API.PhysicalFlowParticipantStore.findByPhysicalFlowId,
                [vm.parentEntityRef.id],
                { force: true })
            .then(r => vm.currentParticipantsByKind = _.groupBy(r.data, "kind"));
    }

    vm.$onInit = () => {
        const physicalFlowPromise = serviceBroker
            .loadViewData(
                CORE_API.PhysicalFlowStore.getById,
                [vm.parentEntityRef.id])
            .then(r => vm.physicalFlow = r.data);

        physicalFlowPromise
            .then(() => serviceBroker
                .loadViewData(
                    CORE_API.LogicalFlowStore.getById,
                    [vm.physicalFlow.logicalFlowId]))
            .then(r => vm.logicalFlow = r.data);

        physicalFlowPromise
            .then(() => serviceBroker
                .loadViewData(
                    CORE_API.LogicalFlowStore.findPermissionsForFlow,
                    [vm.physicalFlow.logicalFlowId]))
            .then(r => vm.canEdit = _.some(
                r.data,
                d => _.includes(["ADD", "UPDATE", "REMOVE"], d)));

        reloadParticipants();
    };

    vm.onRemoveParticipant = p => {
        return serviceBroker
            .execute(
                CORE_API.PhysicalFlowParticipantStore.remove,
                [ vm.physicalFlow.id, p.kind, p.participant ])
            .then(() => {
                toasts.success("Participant removed");
                reloadParticipants();
            })
            .catch(e => displayError("Failed to add participant" , e));
    };

    vm.onAddSourceParticipant = s => onAddParticipant(s, "SOURCE");
    vm.onAddTargetParticipant = s => onAddParticipant(s, "TARGET");
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
    id: "waltzPhysicalFlowParticipantsSection"
};
