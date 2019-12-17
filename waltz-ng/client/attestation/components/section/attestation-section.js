/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
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

import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";
import template from "./attestation-section.html";

const initialState = {
    attestations: [],
    createType: null,
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
            return confirm("This application is connected to unknown and / or deprecated data types, please update these flows before the attestation can be updated.")
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


