
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

function service($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/change-initiative`;


    const findByRef = (kind, id) => $http
            .get(`${BASE}/ref/${kind}/${id}`)
            .then(r => r.data);


    const getById = (id) => $http
            .get(`${BASE}/id/${id}`)
            .then(r => r.data);


    const findRelatedForId = (id) => $http
            .get(`${BASE}/id/${id}/related`)
            .then(r => r.data);


    const search = (query) => $http
            .get(`${BASE}/search/${query}`)
            .then(r => r.data);


    return {
        findByRef,
        findRelatedForId,
        getById,
        search
    }
}

service.$inject = ['$http', 'BaseApiUrl'];

export default service;
