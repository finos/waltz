
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

function service(DataFlowUtilityService, $q, $http, BaseApiUrl, appStore) {

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


    const findEnrichedFlowsForApplication = (id) => {
        const acc = {};

        return findByEntityReference('APPLICATION', id)
            .then(flows => {
                acc.flows = flows;
                return _.chain(flows)
                    .map(f => [f.source.id, f.target.id])
                    .flatten()
                    .uniq()
                    .value()
            })
            .then(appIds => appStore.findByIds(appIds))
            .then(apps => DataFlowUtilityService.enrich(acc.flows, apps));
    };


    const create = (flow) => $http.post(BASE, flow);


    const findByAppIds = (appIds) =>
        $http.post(`${BASE}/apps`, appIds)
            .then(r => { console.log('tap', r); return r; })
            .then(r => r.data);

    return {
        findByEntityReference,
        findByOrgUnit,
        findByOrgUnitTree,
        findEnrichedFlowsForApplication,
        findByAppIds,
        create
    };
}

service.$inject = [
    'DataFlowUtilityService',
    '$q',
    '$http',
    'BaseApiUrl',
    'ApplicationStore',
];


export default service;
