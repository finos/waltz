
/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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


    const recalculateAll = () =>
        $http
            .get(`${BASE}/recalculate-flow-ratings`)
            .then(r => r.data);


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
        recalculateAll,
        remove
    };

};

service.$inject = ['$http', 'BaseApiUrl'];

export default service;
