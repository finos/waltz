
/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import _ from "lodash";


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


    const findApplicationCapabilitiesByAppIdSelector = options =>
        http.post(`${BASE}/selector`, options)
            .then(result => result.data);


    const findByCapabilityIds = ids =>
        http.post(`${BASE}/capability`, ids)
            .then(result => result.data);


    const countByCapabilityId = () =>
        http.get(`${BASE}/count-by/capability`)
            .then(result => result.data);


    const removeCapability = (applicationId, capabilityId) => {
        return http.delete(`${BASE}/application/${applicationId}/${capabilityId}`)
            .then(result => result.data);
    };


    const save = (applicationId, command) => {
        return http.post(`${BASE}/application/${applicationId}`, command)
            .then(result => result.data);
    };


    return {
        findCapabilitiesByApplicationId,
        findApplicationsByCapabilityId,
        findAssociatedApplicationCapabilitiesByCapabilityId,
        findAssociatedCapabilitiesByApplicationId,
        findApplicationCapabilitiesByAppIdSelector,
        findByCapabilityIds,
        countByCapabilityId,
        removeCapability,
        save
    };

};


service.$inject = ['$http', 'BaseApiUrl'];


export default service;
