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

import {CORE_API} from "../../../common/services/core-api-utils";
import {initialiseData} from "../../../common";
import {executionStatus} from "../../../common/services/enums/execution-status";
import {displayError} from "../../../common/error-utils";

import template from "./change-unit-section.html";
import * as _ from "lodash";


const bindings = {
    parentEntityRef: "<",
};


const initialState = {
    selectedChangeUnit: null
};


function mkExecutionStatusUpdateCommand(cu, targetStatus) {
    return {
        id: cu.id,
        executionStatus: {
            newVal: targetStatus,
            oldVal: cu.executionStatus
        }
    };
}


function controller(notification, serviceBroker, $q) {
    const vm = initialiseData(this, initialState);

    const loadData = (force = false) => {

        const physicalChangeUnitPromise = serviceBroker
            .loadViewData(CORE_API.ChangeUnitViewService.findPhysicalFlowChangeUnitsByChangeSetId,
                [vm.parentEntityRef.id])
            .then(r => vm.physicalFlowChangeUnits = r.data)
            .then(() =>  _.forEach(vm.physicalFlowChangeUnits, cu => {

                const ratingNames = _.uniq(_.flatMap(cu.assessments, changeUnit => changeUnit.rating.name));
                return cu.toDisplayAssessments = (ratingNames.length > 0) ? ratingNames.join(", ") : "n/a";
            }))
            .then(() => vm.physicalFlowColumnDefs = preparePhysicalFlowColumnDefs());


        return $q.all([physicalChangeUnitPromise])
            .then(() => vm.changeUnits = _.map(vm.physicalFlowChangeUnits, cu => cu.changeUnit))
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


    vm.onSelect = (row) => {

        if (!_.isEmpty(row)) {
            vm.selectedChangeUnit = row.changeUnit
        } else {
            vm.selectedChangeUnit = null;
        }
    }

}


function preparePhysicalFlowColumnDefs() {
    return [
        {
            field: "changeUnit.action",
            name: "Action",
            width: "10%",
            cellFilter: "toDisplayName:'changeAction'"
        },
        {
            field: "changeUnit.executionStatus",
            name: "Status",
            width: "10%",
            cellFilter: "toDisplayName:'executionStatus'"
        },
        {
            field: "physicalSpecification.name",
            name: "Physical Specification",
            width: "30%"
        },
        {
            field: "logicalFlow.source.name",
            name: "Source",
            width: "15%"
        },
        {
            field: "logicalFlow.target.name",
            name: "Target",
            width: "15%"
        },
        {
            field: "toDisplayAssessments",
            name: "Assessments",
            width: "20%"
        }
    ];
}


controller.$inject = [
    "Notification",
    "ServiceBroker",
    "$q"
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
