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

import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";
import template from "./attestation-section.html";
import {attest} from "../../attestation-utils";


const bindings = {
    parentEntityRef: "<"
};


const modes = {
    EDIT: "EDIT",
    VIEW: "VIEW"
};


const initialState = {
    attestations: [],
    attestationSections: [],
    mode: modes.VIEW,
    activeAttestationSection: null
};



function mkAttestationData(attestationRuns = [], attestationInstances = []){
    const runsById = _.keyBy(attestationRuns, "id");

    return _.chain(attestationInstances)
        .filter(instance => !_.isEmpty(runsById[instance.attestationRunId]))
        .map(instance => { return {
            "instance": instance,
            "run": runsById[instance.attestationRunId]
        }})
        .value();
}


function mkAttestationSections(baseSections = [], attestations = [], unattestedChanges = []) {
    const unattestedChangesByChildKind = _.groupBy(unattestedChanges, d => d.childKind);

    const attestationsByKind = _
        .chain(attestations)
        .filter(d => d.instance.attestedAt != null)
        .sortBy(d => d.instance.attestedAt)
        .groupBy(d => d.run.attestedEntityKind)
        .value();

    return _
        .chain(baseSections)
        .map(s => {
            const latestAttestation = _.findLast(attestationsByKind[s.type]);
            return {
                section: s,
                latestAttestation: latestAttestation,
                unattestedChanges: _.get(unattestedChangesByChildKind, [s.type], [])
            };
        })
        .value();
}


function controller($q,
                    serviceBroker,
                    notification) {

    const vm = initialiseData(this, initialState);
    const today = new Date();
    const baseSections = [
        {
            type: "LOGICAL_DATA_FLOW",
            name: "Logical Flow - latest attestation",
            actionLabel:  "Attest logical flows",
            typeName: "Logical Flows",
            unattestedChanges: []
        },
        {
            type: "PHYSICAL_FLOW",
            name: "Physical Flow - latest attestation",
            actionLabel:  "Attest physical flows",
            typeName: "Physical Flows",
            unattestedChanges: []
        }
    ];


    const loadAttestationData = (entityReference) => {
        const runsPromise = serviceBroker
            .loadViewData(
                CORE_API.AttestationRunStore.findByEntityRef,
                [entityReference],
                { force: true })
            .then(r => r.data);

        const instancesPromise = serviceBroker
            .loadViewData(
                CORE_API.AttestationInstanceStore.findByEntityRef,
                [entityReference],
                { force: true })
            .then(r => r.data);

        const unattestedChangesPromise = serviceBroker
            .loadViewData(
                CORE_API.ChangeLogStore.findUnattestedChangesByEntityReference,
                [entityReference],
                { force: true })
            .then(r => r.data);

        const permissionGroupPromise = serviceBroker
            .loadViewData(
                CORE_API.PermissionGroupStore.findByEntity,
                [entityReference])
            .then(r => r.data);

        return $q
            .all([runsPromise, instancesPromise, unattestedChangesPromise, permissionGroupPromise])
            .then(([runs, instances, unattestedChanges, permissions]) => {
                vm.attestations = mkAttestationData(runs, instances);
                vm.attestationSections = mkAttestationSections(baseSections, vm.attestations, unattestedChanges);
                vm.permissions = permissions;
            });
    };


    vm.$onChanges = () => {
        if (! vm.parentEntityRef) {
            return;
        }
        loadAttestationData(vm.parentEntityRef);
    };


    vm.attestEntity = () => {
        if (confirm("By clicking confirm, you are attesting that all data flows are present and correct for this entity, and thereby accountable for this validation.")){
            attest(serviceBroker, vm.parentEntityRef, vm.activeAttestationSection.type)
                .then(() => {
                    notification.success("Attested successfully");
                    loadAttestationData(vm.parentEntityRef);
                    vm.onCancelAttestation();
                });
        }
    };

    vm.onInitiateAttestation = (section) => {
        vm.activeAttestationSection = section;
        vm.mode = modes.EDIT;
    };

    vm.hasPermissionToAttest = (entityKind) => {
        return _.isEmpty(vm.permissions)
            ? false
            : _.some(vm.permissions, p => p.qualifierKind === entityKind);
    };

    vm.onCancelAttestation = () => {
        vm.activeAttestationSection = null;
        vm.mode = modes.VIEW;
    };


    vm.outOfDate = (dueDate) => {
        const plannedDate = new Date(dueDate);
        return today > plannedDate;
    };

    vm.determinePopoverText = (attestation) => {
        const outOfDate = vm.outOfDate(attestation.run.dueDate);

        if (attestation.instance.attestedAt) {
            return "This attestation has been completed"
        } else if (outOfDate){
            return "This attestation has not been completed and is now overdue"
        } else {
            return "This attestation has not been completed"
        }
    };

}


controller.$inject = [
    "$q",
    "ServiceBroker",
    "Notification"
];


const component = {
    bindings,
    template,
    controller
};


export default {
    component,
    id: "waltzAttestationSection",
    controllerAs: "$ctrl"
};


