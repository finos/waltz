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
import { mkEntityLinkGridCell } from "../../../common/grid-utils";
import { initialiseData } from "../../../common";

import template from "./physical-flow-participants-section.html";
import {displayError} from "../../../common/error-utils";


const bindings = {
    parentEntityRef: "<",
    onInitialise: "<"
};


const initialState = {
};


function controller(serviceBroker, notification) {
    const vm = initialiseData(this, initialState);

    const onAddParticipant = (s, kind) => {
        return serviceBroker
            .execute(
                CORE_API.PhysicalFlowParticipantStore.add,
                [ vm.physicalFlow.id, kind, { kind: "SERVER", id: s.id } ])
            .then(() => {
                notification.success("Participant added");
                reloadParticipants();
            })
            .catch(e => displayError(notification, "Failed to add participant" , e));
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
                    [ vm.physicalFlow.logicalFlowId ]))
            .then(r => vm.logicalFlow = r.data);

        reloadParticipants();
    };

    vm.onRemoveParticipant = p => {
        return serviceBroker
            .execute(
                CORE_API.PhysicalFlowParticipantStore.remove,
                [ vm.physicalFlow.id, p.kind, p.participant ])
            .then(() => {
                notification.success("Participant removed");
                reloadParticipants();
            })
            .catch(e => displayError(notification, "Failed to add participant" , e));
    };

    vm.onAddSourceParticipant = s => onAddParticipant(s, "SOURCE");
    vm.onAddTargetParticipant = s => onAddParticipant(s, "TARGET");
}


controller.$inject = [
    "ServiceBroker",
    "Notification"
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
