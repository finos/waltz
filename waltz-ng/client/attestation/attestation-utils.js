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


function mkAttestationCommand(parentRef, attestedEntityKind, attestedEntityId){
    return {
        entityReference: parentRef,
        attestedEntityKind: attestedEntityKind,
        attestedEntityId: attestedEntityId
    };
}


/**
 * Sends an attestation to the server and returns a promise.
 *
 * @param serviceBroker
 * @param parentEntityRef
 * @param attestedEntityKind
 * @paran attestedEntityId (optional)
 * @return Promise
 */
export function attest(serviceBroker, parentEntityRef, attestedEntityKind, attestedEntityId) {

    const attestationCommand = mkAttestationCommand(
        parentEntityRef,
        attestedEntityKind,
        attestedEntityId);

    return serviceBroker
        .execute(
            CORE_API.AttestationInstanceStore.attestEntityForUser,
            [attestationCommand]);
}


/**
 * Returns grid data with application, latest attestation, attested status
 * @param applications: all applications subjected for aggregation
 * @param attestationInstances: all attestationInstances for the applications
 * @param displayNameService:  converts codes to nice names
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
    mkEntityLinkGridCell("Name", "appRef", "left", "right"),
    {field: "appAssetCode", name: "Asset Code"},
    {field: "appKind", name: "Kind", cellFilter: "toDisplayName:'applicationKind'"},
    {field: "appCriticality", name: "Business Criticality", cellFilter: "toDisplayName:'criticality'"},
    {field: "appLifecyclePhase", name: "Lifecycle Phase", cellFilter: "toDisplayName:'lifecyclePhase'"},
    {field: "attestedBy", name: "Last Attested By"},
    mkDateGridCell("Last Attested At", "attestedAt", false, true)
];
