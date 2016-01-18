
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

import _ from 'lodash';


function toEntityRef(capability) {
    return {
        id: capability.id,
        name: capability.name,
        kind: 'CAPABILITY'
    };
}


const service = (http, root) => {

    const BASE = `${root}/app-capability`;


    const findCapabilitiesByApplicationId = id =>
        http.get(`${BASE}/application/${id}`)
            .then(result => result.data);


    const findApplicationsByCapabilityId = id =>
        http.get(`${BASE}/capability/${id}`)
            .then(result => result.data);


    const findAssociatedApplicationCapabilitiesByCapabilityId = id =>
        http.get(`${BASE}/capability/${id}/associated`)
            .then(result => result.data);


    const findAssociatedCapabilitiesByApplicationId = id =>
        http.get(`${BASE}/application/${id}/associated`)
            .then(result => result.data);


    const findApplicationCapabilitiesForOrgUnitId = id =>
        http.get(`${BASE}/org-unit/${id}`)
            .then(result => result.data);


    const findApplicationCapabilitiesForOrgUnitTree = id =>
        http.get(`${BASE}/org-unit-tree/${id}`)
            .then(result => result.data);


    const countByCapabilityId = () =>
        http.get(`${BASE}/count-by/capability`)
            .then(result => result.data);


    const update = (id, request) => {
        const cleanedRequest = {
            additions: _.map(request.additions, toEntityRef),
            removals: _.map(request.removals, toEntityRef)
        };
        return http.post(`${BASE}/application/${id}`, cleanedRequest);
    };

    const removeCapability = (applicationId, capabilityId) => {
        return http.delete(`${BASE}/application/${applicationId}/${capabilityId}`)
            .then(result => result.data);
    };

    const addCapability = (applicationId, capabilityId) => {
        return http.post(`${BASE}/application/${applicationId}/${capabilityId}`)
            .then(result => result.data);
    };


    const setIsPrimary = (applicationId, capabilityId, isPrimary) => {
        return http.post(`${BASE}/application/${applicationId}/${capabilityId}/primary`, isPrimary)
            .then(result => result.data);
    };


    return {
        findCapabilitiesByApplicationId,
        findApplicationsByCapabilityId,
        findAssociatedApplicationCapabilitiesByCapabilityId,
        findAssociatedCapabilitiesByApplicationId,
        findApplicationCapabilitiesForOrgUnitId,
        findApplicationCapabilitiesForOrgUnitTree,
        countByCapabilityId,
        removeCapability,
        addCapability,
        setIsPrimary,
        update
    };

};

service.$inject = ['$http', 'BaseApiUrl'];

export default service;
