/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
 */

function service($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/orphan`;


    const findAppsWithNonExistentOrgUnits = () => $http
        .get(`${BASE}/application-non-existing-org-unit`)
        .then(result => result.data);


    const findOrphanAppCaps = () => $http
        .get(`${BASE}/application-capability`)
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


    const findOrphanLogicalDataFlows = () => $http
        .get(`${BASE}/logical-data-flow`)
        .then(result => result.data);


    return {
        findAppsWithNonExistentOrgUnits,
        findOrphanAppCaps,
        findOrphanAuthoritativeSourcesByApp,
        findOrphanAuthoritativeSourcesByOrgUnit,
        findOrphanAuthoritativeSourcesByDataType,
        findOrphanChangeInitiatives,
        findOrphanLogicalDataFlows
    };

}


service.$inject = [
    '$http',
    'BaseApiUrl'
];


export default service;

