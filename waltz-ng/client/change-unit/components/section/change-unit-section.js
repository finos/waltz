/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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
    selectedChangeUnit: null,
    physicalFlowColumnDefs: preparePhysicalFlowColumnDefs()
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


function mkAssessmentValuesString(changeUnit) {
    const ratingNames = _
        .chain(changeUnit.assessments)
        .map(a => a.ratingDefinition.name)
        .uniq()
        .join(", ")
        .value();

    return ratingNames || "n/a";
}


function controller(notification, serviceBroker, $q) {
    const vm = initialiseData(this, initialState);

    const loadData = (force = false) => {
        const physicalFlowChangeUnitPromise = serviceBroker
            .loadViewData(
                CORE_API.ChangeUnitViewService.findPhysicalFlowChangeUnitsByChangeSetId,
                [vm.parentEntityRef.id],
                { force: true })
            .then(r => {
                const extendChangeUnitWithRatings = cu =>
                    Object.assign({}, cu, { assessmentValues: mkAssessmentValuesString(cu) });

                vm.physicalFlowChangeUnits =_.map(r.data, extendChangeUnitWithRatings);
            });

        return $q
            .all([physicalFlowChangeUnitPromise])
            .then(() => vm.changeUnits = _.map(vm.physicalFlowChangeUnits, cu => cu.changeUnit))
            .then(() => {
                if(vm.selectedChangeUnit) {
                    vm.selectedChangeUnit = _.find(vm.changeUnits, cu => cu.id === vm.selectedChangeUnit.id);
                }
            })
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
            field: "assessmentValues",
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
