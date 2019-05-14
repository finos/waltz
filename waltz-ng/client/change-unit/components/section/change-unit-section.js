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

import { CORE_API } from "../../../common/services/core-api-utils";
import { initialiseData } from "../../../common";
import { executionStatus } from "../../../common/services/enums/execution-status";
import { displayError } from "../../../common/error-utils";

import template from "./change-unit-section.html";


const bindings = {
    parentEntityRef: "<",
};


const initialState = {};


function mkExecutionStatusUpdateCommand(cu, targetStatus) {
    return {
        id: cu.id,
        executionStatus: {
            newVal: targetStatus,
            oldVal: cu.executionStatus
        }
    };
}


function controller(notification, serviceBroker) {
    const vm = initialiseData(this, initialState);

    const loadData = (force = false) => {
        let cuPromise = null;
        if(vm.parentEntityRef.kind === "CHANGE_SET") {
            //load for change set id
            cuPromise = serviceBroker
                .loadViewData(CORE_API.ChangeUnitStore.findByChangeSetId, [vm.parentEntityRef.id], {force})
                .then(r => r.data);
        } else {
            // load by subject ref
            cuPromise = serviceBroker
                .loadViewData(CORE_API.ChangeUnitStore.findBySubjectRef, [vm.parentEntityRef], {force})
                .then(r => r.data);
        }
        return cuPromise
            .then(changeUnits => vm.changeUnits = changeUnits);
    };


    vm.$onChanges = (changes) => {
        if(changes.parentEntityRef) {
            loadData()
        }
    };


    vm.completeChangeUnit = (cu) => {
        if (confirm("Are you sure you wish to complete this change?  Note: changes will be applied to Waltz current state")) {
            const cmd = mkExecutionStatusUpdateCommand(cu, executionStatus.COMPLETE.key);
            serviceBroker.execute(CORE_API.ChangeUnitStore.updateExecutionStatus, [cmd])
                .then(r => r.data)
                .then(() => {
                    loadData(true);
                    notification.success("Change Unit Completed");
                })
                .catch(e => displayError(notification, "Failed to complete change unit", e));
        }

    };


    vm.discardChangeUnit = (cu) => {
        if (confirm("Are you sure you wish to discard this change?")) {
            const cmd = mkExecutionStatusUpdateCommand(cu, executionStatus.DISCARDED.key);
            serviceBroker.execute(CORE_API.ChangeUnitStore.updateExecutionStatus, [cmd])
                .then(r => r.data)
                .then(() => {
                    loadData(true);
                    notification.success("Change Unit Discarded");
                })
                .catch(e => displayError(notification, "Failed to discard change unit", e));
        }
    };
}


controller.$inject = [
    "Notification",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzChangeUnitSection"
};
