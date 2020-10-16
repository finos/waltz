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
import {mkDateGridCell, mkEntityLinkGridCell} from "../common/grid-utils";
import {mapToDisplayNames} from "../applications/application-utils";
import {entity} from "../common/services/enums/entity";


function mkAttestationCommand(attestedEntityRef, attestationKind){
    return {
        entityReference: attestedEntityRef,
        attestedEntityKind: attestationKind
    };
}


/**
 * Sends an attestation to the server and returns a promise.
 *
 * @param serviceBroker
 * @param attestedEntityRef
 * @param attestationKind
 * @return Promise
 */
export function attest(serviceBroker, attestedEntityRef, attestationKind) {
    const attestationCommand = mkAttestationCommand(
        attestedEntityRef,
        attestationKind);
    return serviceBroker
        .execute(
            CORE_API.AttestationInstanceStore.attestEntityForUser,
            [attestationCommand]);
}


/**
 * Logical flows are unattestable if they have unknown data types
 * and/or deprecated data types.
 *
 * Currently only upstream flows (with respect to the endpointRef)
 * are considered.
 *
 * @param endpointRef  which app we are attesting on behalf of, used
 *      to give us the directionality of the flow
 * @param logicalFlows  set of all logical flows to be checked
 * @param dataTypes  all dataTypes
 * @param flowDecorators  all decorations for the above flows
 * @return {*}
 */
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


/**
 * Logical flows are unattestable if they have unknown data types
 * and/or deprecated data types.
 *
 * Currently only upstream flows (with respect to the endpoint given
 * in the selector)
 * are considered.
 *
 * @param $q  service to combine promises
 * @param serviceBroker  service to communicate to Waltz server
 * @param selector  selector (typicall app) of the entity being attested to
 * @return {*}
 */
export function loadAndCalcUnattestableLogicalFlows($q, serviceBroker, selector) {

    const logicalFlowsPromise = serviceBroker
        .loadViewData(
            CORE_API.LogicalFlowStore.findByEntityReference,
            [selector.entityReference],
            { force: true })
        .then(r => r.data);

    const logicalFlowDecoratorPromise = serviceBroker
        .loadViewData(
            CORE_API.DataTypeDecoratorStore.findBySelector,
            [selector, entity.LOGICAL_DATA_FLOW.key ],
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





/**
 * Returns grid data with application, latest attestation, attested status
 * @param applications: all applications subjected for aggregation
 * @param attestationInstances: all attestationInstances for the applications
 * @param attestedEntityKind: Entity Kind against which attestation data needs to be collected
 * **/
export function mkAttestationSummaryDataForApps(applications = [],
                                                attestationInstances = [],
                                                displayNameService) {

    const attestationByAppId = _.groupBy(
        attestationInstances,
        "parentEntity.id");

    return _.map(
        applications,
        app => ({
            application: Object.assign({}, app, mapToDisplayNames(displayNameService, app)),
            isAttested: (_.has(attestationByAppId, String(app.id)) ? "ATTESTED" : "NEVER_ATTESTED"),
            attestation: _.maxBy(attestationByAppId[app.id], "attestedAt")
        }));
}


export const attestationSummaryColumnDefs = [
    mkEntityLinkGridCell("Name", "application", "left", "right"),
    {field: "application.assetCode", name: "Asset Code"},
    {field: "application.kindDisplay", name: "Kind"},
    {field: "application.businessCriticalityDisplay", name: "Business Criticality"},
    {field: "application.lifecyclePhaseDisplay", name: "Lifecycle Phase"},
    {field: "attestation.attestedBy", name: "Last Attested By"},
    mkDateGridCell("Last Attested At", "attestation.attestedAt", false, true)
];
