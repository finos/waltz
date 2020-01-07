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

function service($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/orphan`;


    const findAppsWithNonExistentOrgUnits = () => $http
        .get(`${BASE}/application-non-existing-org-unit`)
        .then(result => result.data);


    const findOrphanMeasurableRatings = () => $http
        .get(`${BASE}/measurable-rating`)
        .then(result => result.data);


    const findOrphanAuthoritativeSourcesByApp = () => $http
        .get(`${BASE}/authoritative-source/application`)
        .then(result => result.data);


    const findOrphanAuthoritativeSourcesByOrgUnit = () => $http
        .get(`${BASE}/authoritative-source/org-unit`)
        .then(result => result.data);


    const findOrphanAuthoritativeSourcesByDataType = () => $http
        .get(`${BASE}/authoritative-source/data-type`)
        .then(result => result.data);


    const findOrphanChangeInitiatives = () => $http
        .get(`${BASE}/change-initiative`)
        .then(result => result.data);


    const findOrphanLogicalFlows = () => $http
        .get(`${BASE}/logical-flow`)
        .then(result => result.data);


    const findOrphanPhysicalFlows = () => $http
        .get(`${BASE}/physical-flow`)
        .then(result => result.data);


    const findOrphanAttestations = () => $http
        .get(`${BASE}/attestation`)
        .then(result => result.data);


    return {
        findAppsWithNonExistentOrgUnits,
        findOrphanMeasurableRatings,
        findOrphanAuthoritativeSourcesByApp,
        findOrphanAuthoritativeSourcesByOrgUnit,
        findOrphanAuthoritativeSourcesByDataType,
        findOrphanChangeInitiatives,
        findOrphanLogicalFlows,
        findOrphanPhysicalFlows,
        findOrphanAttestations
    };

}


service.$inject = [
    '$http',
    'BaseApiUrl'
];


export default service;

