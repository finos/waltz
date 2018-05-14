

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
    const base = BaseApiUrl + '/rating-scheme';


    function findAll(id) {
        return $http
            .get(base)
            .then(result => result.data);
    }

    function getById(id) {
        return $http
            .get(`${base}/id/${id}`)
            .then(result => result.data);
    }


    return {
        findAll,
        getById
    };
}

store.$inject = ['$http', 'BaseApiUrl'];


export const serviceName = 'RatingSchemeStore';

export const RatingSchemeStore_API = {
    findAll: {
        serviceName,
        serviceFnName: 'findAll',
        description: 'executes findAll'
    },
    getById: {
        serviceName,
        serviceFnName: 'getById',
        description: 'executes getById'
    }
};

