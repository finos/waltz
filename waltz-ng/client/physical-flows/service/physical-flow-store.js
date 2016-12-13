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

import {checkIsEntityRef, checkIsIdSelector, checkIsCreatePhysicalFlowCommand} from "../../common/checks";


function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/physical-flow`;


    const findByEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${base}/entity/${ref.kind}/${ref.id}`)
            .then(r => r.data);
    };


    const findByProducerEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${base}/entity/${ref.kind}/${ref.id}/produces`)
            .then(r => r.data);
    };


    const findByConsumerEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${base}/entity/${ref.kind}/${ref.id}/consumes`)
            .then(r => r.data);
    };


    const findBySpecificationId = (id) => {
        return $http
            .get(`${base}/specification/${id}`)
            .then(r => r.data);
    };

    const getById = (id) => {
        return $http
            .get(`${base}/id/${id}`)
            .then(r => r.data);
    };


    const findBySelector = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${base}/selector`, options)
            .then(r => r.data);
    };


    const create = (cmd) => {
        checkIsCreatePhysicalFlowCommand(cmd);
        return $http
            .post(base, cmd)
            .then(r => r.data);
    };


    const searchReports = (query) => {
        return $http
            .get(`${base}/search-reports/${query}`)
            .then(r => r.data);
    };


    const deleteById = (id) => $http
            .delete(`${base}/${id}`)
            .then(r => r.data);


    return {
        findBySpecificationId,
        findByEntityReference,
        findByProducerEntityReference,
        findByConsumerEntityReference,
        findBySelector,
        getById,
        searchReports,
        create,
        deleteById
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export default store;