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

const initialState = {
    attestations: [],
    createType: null
};

const bindings = {
    parentEntityRef: "<"
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


function controller($q,
                    serviceBroker,
                    notification) {

    const vm = initialiseData(this, initialState);

    const loadData = () => {

        const runsPromise = serviceBroker
            .loadViewData(CORE_API.AttestationRunStore.findByEntityRef,
                [vm.parentEntityRef],
                { force: true })
            .then(r => r.data);

        const instancesPromise = serviceBroker
            .loadViewData(CORE_API.AttestationInstanceStore.findByEntityRef,
                [vm.parentEntityRef],
                { force: true })
            .then(r => r.data);

        const logicalFlowsPromise = serviceBroker
            .loadViewData(CORE_API.LogicalFlowStore.findByEntityReference,
                [vm.parentEntityRef],
                {force: true})
            .then(r => r.data);

        const logicalFlowDecoratorPromise = serviceBroker
            .loadViewData(CORE_API.LogicalFlowDecoratorStore.findBySelectorAndKind,
                [{
                    entityReference: vm.parentEntityRef,
                    scope: 'EXACT'
                }, 'DATA_TYPE'])
            .then(r => r.data);

        const dataTypePromise = serviceBroker
            .loadViewData(CORE_API.DataTypeStore.findAll)
            .then(r => r.data);


        const sections = [
            {
                type: "LOGICAL_DATA_FLOW",
                name: "Logical Flow - latest attestation",
                attestMessage:  "Attest logical flows"
            },
            {
                type: "PHYSICAL_FLOW",
                name: "Physical Flow - latest attestation",
                attestMessage:  "Attest physical flows"
            }
        ];

        return $q.all([runsPromise, instancesPromise, logicalFlowsPromise, logicalFlowDecoratorPromise, dataTypePromise])
            .then(([runs, instances, logicalFlows, flowDecorators, dataTypes]) => {

                vm.attestations = mkAttestationData(runs, instances);

                const attestationsByKind = _
                    .chain(vm.attestations)
                    .filter(d => d.instance.attestedAt != null)
                    .sortBy(d => d.instance.attestedAt)
                    .groupBy(d => d.run.attestedEntityKind)
                    .value();

                vm.groupedAttestations = _
                    .chain(sections)
                    .map(s => {
                        const latestAttestation = _.findLast(attestationsByKind[s.type]);
                        return {
                            section: s,
                            latestAttestation: latestAttestation
                        };
                    })
                    .value();

                const upstreamFlowIds = _.chain(logicalFlows)
                    .filter(flow => flow.target.id === vm.parentEntityRef.id)
                    .map(flow => flow.id)
                    .value();

                const unknownOrDeprecatedDatatypeIds = _.chain(dataTypes)
                    .filter(dt => dt.deprecated === true || dt.unknown === true)
                    .map(dt => dt.id)
                    .value();

                vm.upstreamFlowsWithUnknownOrDeprecatedDataTypes = _.filter(flowDecorators,
                        d =>
                            _.includes(upstreamFlowIds, d.dataFlowId)
                            && _.includes(unknownOrDeprecatedDatatypeIds, d.decoratorEntity.id));

            });
    };

    vm.$onInit = () => {
        loadData();
    };

    vm.$onChanges = () => {
        if (vm.parentEntityRef) {
            loadData();
        }
    };

    vm.setCreateType = (type) => vm.createType = type;

    vm.attestEntity = () => {

        if(vm.upstreamFlowsWithUnknownOrDeprecatedDataTypes.length !== 0 && vm.createType === 'LOGICAL_DATA_FLOW'){
            confirm("This application is connected to unknown and / or deprecated data types, please update these flows before the attestation can be updated.");
            return notification.error("Flows cannot be attested. Please update all unknown and deprecated datatypes before attesting logical flows.");
        }

        if (confirm("By clicking confirm, you are attesting that all data flows are present and correct for this entity, and thereby accountable for this validation.")){
            return serviceBroker
                .execute(CORE_API.AttestationInstanceStore.attestEntityForUser,
                    [mkCreateCommand(vm.parentEntityRef, vm.createType)])
                .then(() => notification.success("Attested successfully"))
                .then(() => loadData())
                .then(() => vm.setCreateType(null));
        }
    };

    vm.cancelAttestation = () => {
        vm.setCreateType(null);
    };

    const today = new Date();

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
    }

}


function mkCreateCommand(parentEntityRef, entityKind){

    return {
        entityReference: parentEntityRef,
        attestedEntityKind: entityKind};
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


