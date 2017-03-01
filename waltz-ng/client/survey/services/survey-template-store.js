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

function service($http, baseUrl) {

    const BASE = `${baseUrl}/survey-template`;

    const create = (cmd) => {
        return $http.post(`${BASE}`, cmd)
            .then(result => result.data);
    };

    const getById = (id) => {
        return $http.get(`${BASE}/${id}`)
            .then(result => result.data);
    };

    const findActive = () => {
        return $http.get(`${BASE}/active`)
            .then(result => result.data);
    };

    const update = (cmd) => {
        return $http.put(`${BASE}`, cmd)
            .then(result => result.data);
    };

    return {
        create,
        getById,
        findActive,
        update
    };
}


service.$inject = [
    '$http',
    'BaseApiUrl'
];


export default service;
