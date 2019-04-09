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


function store($http, baseApiUrl) {
    const baseUrl = `${baseApiUrl}/allocation`;

    const findByEntityAndScheme = (ref, schemeId) => $http
            .get(`${baseUrl}/entity-ref/${ref.kind}/${ref.id}/${schemeId}`)
            .then(d => d.data);

    const findByMeasurableAndScheme = (measurableId, schemeId) => $http
            .get(`${baseUrl}/measurable/${measurableId}/${schemeId}`)
            .then(d => d.data);


    const updateFixedAllocations = (ref, schemeId, fixedAllocations) => $http
        .post(`${baseUrl}/entity-ref/${ref.kind}/${ref.id}/${schemeId}/fixed-allocations`, fixedAllocations)
        .then(d => d.data);

    return {
        findByEntityAndScheme,
        findByMeasurableAndScheme,
        updateFixedAllocations
    };

}

store.$inject = ["$http", "BaseApiUrl"];


const serviceName = "AllocationStore";

export default {
    serviceName,
    store
};

export const AllocationStore_API = {
    findByEntityAndScheme: {
        serviceName,
        serviceFnName: "findByEntityAndScheme",
        description: "findByEntityAndScheme [ref, schemeId]"
    },
    findByMeasurableAndScheme: {
        serviceName,
        serviceFnName: "findByMeasurableAndScheme",
        description: "findByMeasurableAndScheme [measurableId, schemeId]"
    },
    updateFixedAllocations: {
        serviceName,
        serviceFnName: "updateFixedAllocations",
        description: "updateFixedAllocations [ref, schemeId, measurableId, fixedAllocationList[{measurableId, percentage}]]"
    }
};