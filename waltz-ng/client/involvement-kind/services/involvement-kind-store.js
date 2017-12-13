
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

import {checkIsCreateInvolvementKindCommand} from "../../common/checks";


export function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/involvement-kind`;


    const findAll = () =>
        $http.get(BASE)
            .then(result => result.data);


    const getById = (id) => $http
        .get(`${BASE}/id/${id}`)
        .then(r => r.data);


    /**
     * Creates a new Involvement Kind
     *
     * @param cmd : { name: <str>, description: <str> }
     * @returns {Promise.<TResult>|*}
     */
    const create = (cmd) => {
        checkIsCreateInvolvementKindCommand(cmd);
        return $http
            .post(`${BASE}/update`, cmd)
            .then(r => r.data);
    };


    const update = (cmd) => {
        return $http
            .put(`${BASE}/update`, cmd)
            .then(r => r.data);
    };


    const deleteById = (id) => {
        return $http
            .delete(`${BASE}/${id}`)
            .then(r => r.data);
    };


    return {
        findAll,
        getById,
        create,
        update,
        deleteById
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = 'InvolvementKindStore';


export const InvolvementKindStore_API = {
    findAll: {
        serviceName,
        serviceFnName: 'findAll',
        description: 'executes findAll'
    },
    getById: {
        serviceName,
        serviceFnName: 'getById',
        description: 'executes getById'
    },
    create: {
        serviceName,
        serviceFnName: 'create',
        description: 'executes create'
    },
    update: {
        serviceName,
        serviceFnName: 'update',
        description: 'executes update'
    },
    deleteById: {
        serviceName,
        serviceFnName: 'deleteById',
        description: 'executes deleteById'
    }
};