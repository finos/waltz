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

import template from "./physical-flow-participants-sub-section.html";


const bindings = {
    participantKind: "@",
    currentParticipants: "<",
    nodeRef: "<", // usually an appRef
    onRemove: "<",
    onAdd: "<",
    canEdit: "<"
};


const initialState = {
    visibility: {
        editor: false,
        serverPicker: false
    }
};


function merge(participants = [], servers = []) {
    const serversById = _.keyBy(servers, "id");
    return _
        .chain(participants)
        .filter(p => p.participant.kind === "SERVER")
        .map(p => {
            const server = serversById[p.participant.id];
            return {
                participant: p,
                server
            };
        })
        .value();
}


function calculateAvailableServers(participants = [], allServers = []) {
    const usedServerIds = _
        .chain(participants)
        .filter(p => p.participant.kind === "SERVER")
        .map(p => p.participant.id)
        .value();

    return _.filter(allServers, s => ! _.includes(usedServerIds, s.id));
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        switch (vm.participantKind) {
            case "SOURCE":
                vm.name = "Source System";
                break;
            case "TARGET":
                vm.name = "Target System";
                break;
            default:
                vm.name = "???";
                break;
        }
    };

    vm.$onChanges = () => {
        if (vm.nodeRef) {
            serviceBroker
                .loadViewData(
                    CORE_API.ServerInfoStore.findByAppId,
                    [vm.nodeRef.id])
                .then(r => {
                    vm.allServers = r.data;
                    vm.participants = merge(vm.currentParticipants, vm.allServers);
                    vm.availableServers = calculateAvailableServers(vm.currentParticipants, vm.allServers);
                });
        }
    };

    vm.onShowEditor = () => {
        console.log("Showing editor....")
    };

    vm.onHideServerPicker = () => {
        vm.visibility.serverPicker = false;
    };

    vm.onShowServerPicker = () => {
        vm.visibility.serverPicker = true;
    };

    vm.onAddServer = (s) => {
        vm.onAdd(s)
            .then(() => vm.onHideServerPicker());
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
    id: "waltzPhysicalFlowParticipantsSubSection"
};
