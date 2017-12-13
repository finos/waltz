
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

export function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/physical-spec-data-type`;

    const findBySpecificationId = (id) => {
        return $http.get(`${BASE}/specification/${id}`)
            .then(result => result.data);
    };

    const findBySpecificationSelector = (options) => {
        return $http.post(`${BASE}/specification/selector`, options)
            .then(result => result.data);
    };

    const save = (specId, command) => {
        return $http.post(`${BASE}/specification/${specId}`, command)
            .then(result => result.data);
    };

    return {
        findBySpecificationId,
        findBySpecificationSelector,
        save
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = 'PhysicalSpecDataTypeStore';


export const PhysicalSpecDataTypeStore_API = {
    findBySpecificationId: {
        serviceName,
        serviceFnName: 'findBySpecificationId',
        description: 'finds data types for a given specification id'
    },
    findBySpecificationSelector: {
        serviceName,
        serviceFnName: 'findBySpecificationSelector',
        description: 'finds data types for a given specification id selector'
    },
    save: {
        serviceName,
        serviceFnName: 'save',
        description: 'saves (inserts/deletes) data types for a given specification id'
    }
};

