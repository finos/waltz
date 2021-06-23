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
    mkEntityLinkGridCell("Name", "application", "left", "right"),
    {field: "application.assetCode", name: "Asset Code"},
    {field: "application.kindDisplay", name: "Kind"},
    {field: "application.businessCriticalityDisplay", name: "Business Criticality"},
    {field: "application.lifecyclePhaseDisplay", name: "Lifecycle Phase"},
    {field: "attestation.attestedBy", name: "Last Attested By"},
    mkDateGridCell("Last Attested At", "attestation.attestedAt", false, true)
];
