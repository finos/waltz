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

import _ from 'lodash';
import {checkIsEntityRef, checkIsIdSelector} from '../../common/checks'

function service($http, BaseApiUrl) {
    const BASE = `${BaseApiUrl}/logical-flow`;

    // --- FINDERS ---
    const findBySelector = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${BASE}/selector`, options)
            .then(r => r.data);
    };

    const findByEntityReference = (kind, id) => {
        const entityReference = _.isObject(kind)
            ? kind
            : { kind, id };

        checkIsEntityRef(entityReference);

        return $http
            .get(`${BASE}/entity/${entityReference.kind}/${entityReference.id}`)
            .then(result => result.data);
    };

    const findBySpecificationId = (specId) => $http
        .get(`${BASE}/specification/${specId}`)
        .then(result => result.data);

    // --- STATS ---
    const calculateStats = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${BASE}/stats`, options)
            .then(r => r.data);
    };

    const countByDataType = () => $http
        .get(`${BASE}/count-by/data-type`)
        .then(result => result.data);

    // --- UPDATERS ---
    const removeFlow = (id) => $http
        .delete(`${BASE}/${id}`)
        .then(r => r.data);

    const addFlow = (flow) => $http
        .post(`${BASE}`, flow)
        .then(r => r.data);

    return {
        findBySpecificationId,
        findBySelector,
        findByEntityReference,
        calculateStats,
        countByDataType,
        removeFlow,
        addFlow
    };
}


service.$inject = [
    '$http',
    'BaseApiUrl'
];


export default service;
