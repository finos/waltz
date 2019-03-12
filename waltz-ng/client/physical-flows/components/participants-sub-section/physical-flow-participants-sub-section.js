/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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

import _ from "lodash";
import { CORE_API } from "../../../common/services/core-api-utils";
import { initialiseData } from "../../../common";

import template from "./physical-flow-participants-sub-section.html";


const bindings = {
    participantKind: "@",
    currentParticipants: "<",
    nodeRef: "<", // usually an appRef
    onRemove: "<",
    onAdd: "<"
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
