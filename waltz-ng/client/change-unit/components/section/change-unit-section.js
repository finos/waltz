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
