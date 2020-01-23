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

import {CORE_API} from "../common/services/core-api-utils";
import _ from "lodash";



function mkAttestationCommand(attestedEntityRef, attestationKind){
    return {
        entityReference: attestedEntityRef,
        attestedEntityKind: attestationKind
    };
}


export function attest(serviceBroker, attestedEntityRef, attestationKind) {
    const attestationCommand = mkAttestationCommand(
        attestedEntityRef,
        attestationKind);
    return serviceBroker
        .execute(
            CORE_API.AttestationInstanceStore.attestEntityForUser,
            [attestationCommand]);
}




function calcUnattestableLogicalFlows(endpointRef, logicalFlows, dataTypes, flowDecorators) {
    const upstreamFlowIds = _.chain(logicalFlows)
        .filter(flow => flow.target.id === endpointRef.id)
        .map(flow => flow.id)
        .value();

    const unknownOrDeprecatedDatatypeIds = _.chain(dataTypes)
        .filter(dt => dt.deprecated === true || dt.unknown === true)
        .map(dt => dt.id)
        .value();

    return _.filter(
        flowDecorators,
        d => {
            const isUpstream = _.includes(upstreamFlowIds, d.dataFlowId);
            const isUnknownOrDeprecated = _.includes(unknownOrDeprecatedDatatypeIds, d.decoratorEntity.id);
            return isUpstream && isUnknownOrDeprecated;
        });
}


export function loadAndCalcUnattestableLogicalFlows($q, serviceBroker, selector) {

    const logicalFlowsPromise = serviceBroker
        .loadViewData(
            CORE_API.LogicalFlowStore.findByEntityReference,
            [selector.entityReference],
            { force: true })
        .then(r => r.data);

    const logicalFlowDecoratorPromise = serviceBroker
        .loadViewData(
            CORE_API.LogicalFlowDecoratorStore.findBySelectorAndKind,
            [selector, "DATA_TYPE"],
            { force: true })
        .then(r => r.data);

    const dataTypePromise = serviceBroker
        .loadAppData(CORE_API.DataTypeStore.findAll)
        .then(r => r.data);

    return $q
        .all([logicalFlowsPromise, logicalFlowDecoratorPromise, dataTypePromise])
        .then(([logicalFlows, flowDecorators, dataTypes]) => {
            return calcUnattestableLogicalFlows(
                selector.entityReference,
                logicalFlows,
                dataTypes,
                flowDecorators);
        });
}