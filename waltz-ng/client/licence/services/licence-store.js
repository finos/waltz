/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019  Waltz open source project
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

import { checkIsIdSelector } from "../../common/checks";


export function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/licence`;

    const countApplications = () =>
        $http.get(`${BASE}/count/application`)
            .then(result => result.data);

    const findAll = () =>
        $http.get(`${BASE}/all`)
            .then(result => result.data);

    const findBySelector = (selector) => {
        checkIsIdSelector(selector);
        return $http
            .post(`${BASE}/selector`, selector)
            .then(r => r.data);
    };

    const getById = (id) => $http
        .get(`${BASE}/id/${id}`)
        .then(r => r.data);


    return {
        countApplications,
        findAll,
        findBySelector,
        getById
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


const serviceName = "LicenceStore";


export default {
    serviceName,
    store
};


export const LicenceStore_API = {
    countApplications: {
        serviceName,
        serviceFnName: 'countApplications',
        description: 'executes countApplications'
    },
    findAll: {
        serviceName,
        serviceFnName: 'findAll',
        description: 'executes findAll'
    },
    findBySelector: {
        serviceName,
        serviceFnName: 'findBySelector',
        description: 'executes findBySelector'
    },
    getById: {
        serviceName,
        serviceFnName: 'getById',
        description: 'executes getById'
    },
};
