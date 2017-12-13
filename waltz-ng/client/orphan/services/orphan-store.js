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

