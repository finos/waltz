
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

import {checkIsIdSelector} from "../../common/checks";


const service = ($http, root) => {

    const BASE = `${root}/authoritative-source`;


    const findByKind = kind =>
        $http
            .get(`${BASE}/kind/${kind}`)
            .then(result => result.data);


    const findByReference = (kind, id) =>
        $http
            .get(`${BASE}/kind/${kind}/${id}`)
            .then(result => result.data);


    const findByApp = (id) =>
        $http
            .get(`${BASE}/app/${id}`)
            .then(result => result.data);


    const findByDataTypeIdSelector = (selector) => {
        checkIsIdSelector(selector);
        return $http
            .post(`${BASE}/data-type`, selector)
            .then(result => result.data);
    };


    const calculateConsumersForDataTypeIdSelector = (selector) => {
        checkIsIdSelector(selector);
        return $http
            .post(`${BASE}/data-type/consumers`, selector)
            .then(r => r.data);
    };


    const update = (id, newRating) =>
        $http
            .post(`${BASE}/id/${id}`, newRating);


    const remove = (id) =>
        $http
            .delete(`${BASE}/id/${id}`);


    const insert = (insertRequest) => {
        const { kind, id, dataType, appId, rating} = insertRequest;
        const url = `${BASE}/kind/${kind}/${id}/${dataType}/${appId}`;
        return $http
            .post(url, rating);
    };

    return {
        calculateConsumersForDataTypeIdSelector,
        findByKind,
        findByReference,
        findByApp,
        findByDataTypeIdSelector,
        update,
        insert,
        remove
    };

};

service.$inject = ['$http', 'BaseApiUrl'];

export default service;
