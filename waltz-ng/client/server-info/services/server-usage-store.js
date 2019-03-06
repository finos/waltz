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

export function store($http, baseUrl) {

    const BASE = `${baseUrl}/server-usage`;

    const findByReferencedEntity = (ref) =>
        $http
            .get(`${BASE}/ref/${ref.kind}/${ref.id}`)
            .then(result => result.data);

    const findByServerId = (id) =>
        $http
            .get(`${BASE}/server-id/${id}`)
            .then(result => result.data);

    return {
        findByReferencedEntity,
        findByServerId
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


export const serviceName = "ServerUsageStore";


export const ServerUsageStore_API = {
    findByReferencedEntity: {
        serviceName,
        serviceFnName: "findByReferencedEntity",
        description: "executes findByReferencedEntity (entityRef)"
    },
    findByServerId: {
        serviceName,
        serviceFnName: "findByServerId",
        description: "executes findByServerId (serverId)"
    }
};



export default {
    serviceName,
    store
};