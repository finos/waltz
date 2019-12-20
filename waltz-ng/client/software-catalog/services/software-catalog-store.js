/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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
import _ from "lodash";


export function store($http, BaseApiUrl) {
    const BASE = `${BaseApiUrl}/software-catalog`;

    const findByAppIds = (ids = []) => {
        return $http.post(`${BASE}/apps`, ids)
            .then(r => r.data);
    };

    const findStatsForSelector = (id, kind, scope = "CHILDREN") => {
        const options = _.isObject(id)
            ? id
            : {scope, entityReference: {id, kind}};

        return $http.post(`${BASE}/stats`, options)
            .then(result => result.data);
    };


    const findBySelector = (options) => $http
        .post(`${BASE}/selector`, options)
        .then(x => x.data);


    const getByPackageId = (id) => $http
        .get(`${BASE}/package-id/${id}`)
        .then(r => r.data);

    const getByLicenceId = (id) => $http
        .get(`${BASE}/licence-id/${id}`)
        .then(r => r.data);

    return {
        findByAppIds,
        findBySelector,
        findStatsForSelector,
        getByPackageId,
        getByLicenceId
    };
}

store.$inject = [
    "$http",
    "BaseApiUrl"
];


export const serviceName = "SoftwareCatalogStore";


export const SoftwareCatalogStore_API = {
    findByAppIds: {
        serviceName,
        serviceFnName: "findByAppIds",
        description: "retrieve catalog for a list of app ids"
    },
    findBySelector: {
        serviceName,
        serviceFnName: "findBySelector",
        description: "find software catalog for a given selector options"
    },
    findStatsForSelector: {
        serviceName,
        serviceFnName: "findStatsForSelector",
        description: "find software catalog stats the given selector options"
    },
    getByPackageId: {
        serviceName,
        serviceFnName: 'getByPackageId',
        description: 'executes getByPackageId'
    },
};

