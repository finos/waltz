
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

export default [
    'DataFlowUtilityService',
    '$q',
    '$http',
    'BaseApiUrl',
    (DataFlowUtilityService, $q, $http, BaseApiUrl) => {

        const BASE = `${BaseApiUrl}/data-flows`;


        const findByEntityReference = (kind, id) =>
            $http.get(`${BASE}/entity/${kind}/${id}`)
                .then(result => result.data);


        const findByOrgUnit = (id) =>
            $http.get(`${BASE}/org-unit/${id}`)
                .then(ts => {
                    return ts;
                })
                .then(result => result.data);


        const findByOrgUnitTree = (id) =>
            $http.get(`${BASE}/org-unit-tree/${id}`)
                .then(result => result.data);


        const findApplicationsByEntityReference = (kind, id) =>
            $http.get(`${BASE}/entity/${kind}/${id}/applications`)
                .then(result => result.data);


        const findEnrichedFlowsForApplication = (id) => $q
                .all([
                    findByEntityReference('APPLICATION', id),
                    findApplicationsByEntityReference('APPLICATION', id)
                ])
                .then(([flows, flowApps]) =>
                    DataFlowUtilityService.enrich(flows, flowApps));

        const findByCapability = (capabilityId) =>
            $http.get(`${BASE}/capability/${capabilityId}`)
                .then(r => r.data);

        const create = (flow) => $http.post(BASE, flow);



        return {
            findApplicationsByEntityReference,
            findByEntityReference,
            findByOrgUnit,
            findByOrgUnitTree,
            findEnrichedFlowsForApplication,
            findByCapability,
            create
        };
    }
];
