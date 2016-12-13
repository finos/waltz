
/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import { checkIsCreateActorCommand } from "../../common/checks";


function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/actor`;


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
        checkIsCreateActorCommand(cmd);
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


    const search = (query) => {
        return $http
            .get(`${BASE}/search/${query}`)
            .then(r => r.data || []);
    };


    return {
        findAll,
        getById,
        create,
        update,
        deleteById,
        search
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export default store;
