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

import template from "./planned-decommission-editor.html";
import {initialiseData, invokeFunction} from "../../../common";
import {alignDateToUTC} from "../../../common/date-utils";
import _ from "lodash";

const modes= {
    VIEW: "VIEW",
    SELECT_APP: "SELECT_APP",
    SELECT_COMM_DATE: "SELECT_COMM_DATE",
    CONFIRM_ADDITION: "CONFIRM_ADDITION",
    CONFIRM_REMOVAL: "CONFIRM_REMOVAL"
};


const bindings = {
    measurableRating: "<?",
    plannedDecommission: "<?",
    replacementApps: "<?",
    onSaveDecommissionDate: "<",
    onRemoveDecommission: "<",
    onSaveReplacementApp: "<",
    onRemoveReplacementApp: "<",
    category: "<",
    application: "<?"
};


const initialState = {
    candidateApp: null,
    candidateRemoval: null,
    candidateCommissionDate: null,
    plannedDecommission: null,
    replacementApps: [],
    mode: modes.VIEW
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (c) => {
        if (c.plannedDecommission) {
            vm.mode = modes.VIEW;
        }
    };

    vm.onShowAdd = () => {
        vm.mode = modes.SELECT_APP;
    };

    vm.onCancelAdd = () => {
        if (vm.mode === modes.SELECT_APP) {
            vm.mode = modes.VIEW;
        } else if (vm.mode === modes.SELECT_COMM_DATE) {
            vm.mode = modes.SELECT_APP;
        } else {
            vm.mode = modes.VIEW;
        }
    };

    vm.selectionFilter = (candidate) => {
        const isSelf = candidate.id === vm.measurableRating.entityReference.id;
        if (isSelf) {
            return false;
        } else {
            const existingReplacementApps = _.map(vm.replacementApps, d => d.entityReference.id);
            return !_.includes(existingReplacementApps, candidate.id);
        }
    };

    vm.onSelectReplacementCandidate = (d) => {
        vm.mode = modes.SELECT_COMM_DATE;
        vm.candidateApp = d;
    };

    vm.onSetCommissionDate = (c) => {
        vm.mode = modes.CONFIRM_ADDITION;
        vm.candidateCommissionDate = c.newVal;
    };

    vm.onAddReplacement = () => {
        const replacement = {
            decommissionId: vm.plannedDecommission.id,
            replacementApp: vm.candidateApp,
            commissionDate: alignDateToUTC(vm.candidateCommissionDate)
        };
        invokeFunction(vm.onSaveReplacementApp, replacement);
        vm.mode = modes.VIEW;
    };

    vm.onSelectCandidateForRemoval = (replacement) => {
        vm.candidateRemoval = replacement;
        vm.mode = modes.CONFIRM_REMOVAL;
    };

    vm.onRemoveReplacement = () => {
        invokeFunction(vm.onRemoveReplacementApp, vm.candidateRemoval)
            .then(() => vm.mode = modes.VIEW)
    };

    vm.onSaveCommissionDate = (newDate, ctx) => {
        const replacement = {
            decommissionId: vm.plannedDecommission.id,
            replacementApp: ctx.entityReference,
            commissionDate: alignDateToUTC(newDate.newVal)
        };
        invokeFunction(vm.onSaveReplacementApp, replacement);
        vm.mode = modes.VIEW;
    };
}


controller.$inject = [
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzPlannedDecommissionEditor"
};
