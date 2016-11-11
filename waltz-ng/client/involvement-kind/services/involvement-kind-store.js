
/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
 */

import {checkIsCreateInvolvementKindCommand} from "../../common/checks";


function store($http, BaseApiUrl) {

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


export default store;
