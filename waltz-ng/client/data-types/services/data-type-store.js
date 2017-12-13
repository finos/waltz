
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

function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/data-types`;

    const findAll = () =>
        $http.get(BASE)
            .then(result => result.data);


    const search = (query) => $http
        .get(`${BASE}/search/${query}`)
        .then(r => r.data);


    return {
        findAll,
        search
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


const serviceName = 'DataTypeStore';


export const DataTypeStore_API = {
    findAll: {
        serviceName,
        serviceFnName: 'findAll',
        description: 'finds all data types'
    },
    search: {
        serviceName,
        serviceFnName: 'search',
        description: 'search data types'
    }
};


export default {
    store,
    serviceName
};
