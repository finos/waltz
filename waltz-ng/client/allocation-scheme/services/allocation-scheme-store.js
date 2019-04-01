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
    const baseUrl = `${baseApiUrl}/allocation-scheme`;

    const findAll = () => $http
        .get(`${baseUrl}/all`)
        .then(d => d.data);

    const findByCategory = (id) => $http
            .get(`${baseUrl}/category/${id}`)
            .then(d => d.data);

    const getById = (id) => $http
        .get(`${baseUrl}/id/${id}`)
        .then(x => x.data);

    return {
        findAll,
        findByCategory,
        getById
    };

}

store.$inject = ["$http", "BaseApiUrl"];


const serviceName = "AllocationSchemeStore";

export default {
    serviceName,
    store
};

export const AllocationSchemeStore_API = {
    findAll: {
        serviceName,
        serviceFnName: "findAll",
        description: "finds all schemes"
    },
    findByCategory: {
        serviceName,
        serviceFnName: "findByCategory",
        description: "finds all schemes by measurable category"
    },
    getById: {
        serviceName,
        serviceFnName: "getById",
        description: "retrieves a single scheme"
    }
};