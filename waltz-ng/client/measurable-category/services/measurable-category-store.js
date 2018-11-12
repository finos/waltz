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
    const baseUrl = `${baseApiUrl}/measurable-category`;

    const findAll = () => $http
        .get(`${baseUrl}/all`)
        .then(d => d.data);


    const getById = (id) => $http
        .get(`${baseUrl}/id/${id}`)
        .then(x => x.data);

    return {
        findAll,
        getById
    };

}

store.$inject = ["$http", "BaseApiUrl"];


const serviceName = "MeasurableCategoryStore";

export default {
    serviceName,
    store
};

export const MeasurableCategoryStore_API = {
    findAll: {
        serviceName,
        serviceFnName: "findAll",
        description: "finds all categories"
    },
    getById: {
        serviceName,
        serviceFnName: "getById",
        description: "retrieves a single category"
    }
};