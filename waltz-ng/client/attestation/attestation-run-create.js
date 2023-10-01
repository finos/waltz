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
import {CORE_API} from "../common/services/core-api-utils";
import {formats, initialiseData} from "../common/index";
import moment from "moment";
import template from "./attestation-run-create.html";
import toasts from "../svelte-stores/toast-store";


const exactScope = {
    value: "EXACT",
    name: "Exact"
};


const childrenScope = {
    value: "CHILDREN",
    name: "Children"
};

const initialState = {
    attestationRun: {
        targetEntityKind: "APPLICATION",
        selectorEntityKind: "APP_GROUP",
        selectorScope: "EXACT"
    },
    availableAttestedKinds: [
        "LOGICAL_DATA_FLOW",
        "PHYSICAL_FLOW",
        "MEASURABLE_CATEGORY"
    ],
    targetEntityKinds: [
        {name: "Application", value: "APPLICATION"}
    ],
    allowedEntityKinds: [
        {value: "APP_GROUP", name: "Application Group"},
        {value: "ORG_UNIT", name: "Org Unit"},
        {value: "MEASURABLE", name: "Measurable"}
    ],
    allowedScopes: {
        "APP_GROUP": [exactScope],
        "CHANGE_INITIATIVE": [exactScope],
        "ORG_UNIT": [exactScope, childrenScope],
        "MEASURABLE": [exactScope, childrenScope]
    },
    displaySummary: false,
    loadingSummary: false
};




function mkCreateCommand(attestationRun){
    const involvementKindIds = _.map(attestationRun.involvementKinds, ik => ik.id);
    return {
        name: attestationRun.name,
        description: attestationRun.description,
        selectionOptions: {
            entityReference: {
                kind: attestationRun.selectorEntityKind,
                id: attestationRun.selectorEntity.id
            },
            scope: attestationRun.selectorScope
        },
        targetEntityKind: attestationRun.targetEntityKind,
        attestedEntityKind: attestationRun.attestedEntityKind,
        attestedEntityId: attestationRun.attestedEntityId,
        involvementKindIds: involvementKindIds,
        dueDate: moment(attestationRun.dueDate).format(formats.parseDateOnly)
    };
}


function controller($state,
                    serviceBroker,
                    involvementKindStore) {

    const vm = initialiseData(this, initialState);

    involvementKindStore.findAll()
        .then(
            involvementKinds => {
                vm.availableInvolvementKinds = _.filter(
                    involvementKinds,
                    d => d.subjectKind === vm.attestationRun.targetEntityKind);
            }
        );

    vm.onSelectorEntityKindChange = () => {
        vm.attestationRun.selectorEntity = null;
    };

    vm.onSelectorEntitySelect = (entity) => {
        vm.attestationRun.selectorEntity = entity;
    };

    vm.onAttestedKindChange = () => {
        if(vm.attestationRun.attestedEntityKind === "MEASURABLE_CATEGORY") {
            serviceBroker
                .loadAppData(CORE_API.MeasurableCategoryStore.findAll)
                .then(r => vm.measurableCategories = r.data);
        }

    };

    vm.loadCreateSummary = () => {
        const command = mkCreateCommand(vm.attestationRun);
        vm.loadingSummary = true;
        serviceBroker
            .execute(CORE_API.AttestationRunStore.getCreateSummary, [command])
            .then(res => {
                vm.summary = res.data;
                vm.loadingSummary = false;
                vm.displaySummary = true;
            });
    };

    vm.create = () => {
        const command = mkCreateCommand(vm.attestationRun);

        serviceBroker
            .execute(CORE_API.AttestationRunStore.create, [command])
            .then(res => {
                toasts.success("Attestation run created successfully");
                serviceBroker.loadAppData(CORE_API.NotificationStore.findAll, [], { force: true });
                $state.go("main.attestation.run.view", {id: res.data.id});
            }, () => toasts.error("Failed to create attestation run"))
    };

    vm.cancel = () => {
        vm.displaySummary = false;
        vm.summary = null;
    };
}


controller.$inject = [
    "$state",
    "ServiceBroker",
    "InvolvementKindStore"
];


export default {
    template,
    controller,
    controllerAs: "ctrl"
};

