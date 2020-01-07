/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */


import {checkIsIdSelector} from "../../common/checks";

export function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/physical-spec-definition`;

    const findBySelector = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${base}/selector`, options)
            .then(r => r.data);
    };

    const findForSpecificationId = (specId) => {
        return $http
            .get(`${base}/specification/${specId}`)
            .then(r => r.data);
    };

    const create = (specId, command) => {
        return $http
            .post(`${base}/specification/${specId}`, command)
            .then(r => r.data);
    };

    const updateStatus = (specId, command) => {
        return $http
            .put(`${base}/specification/${specId}/status`, command)
            .then(r => r.data);
    };

    const deleteSpecification = (specId) => {
        return $http
            .delete(`${base}/specification/${specId}`)
            .then(r => r.data);
    };


    return {
        findBySelector,
        findForSpecificationId,
        create,
        updateStatus,
        deleteSpecification
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = 'PhysicalSpecDefinitionStore';


export const PhysicalSpecDefinitionStore_API = {
    findBySelector: {
        serviceName,
        serviceFnName: 'findBySelector',
        description: 'executes findBySelector'
    },
    findForSpecificationId: {
        serviceName,
        serviceFnName: 'findForSpecificationId',
        description: 'executes findForSpecificationId'
    },
    create: {
        serviceName,
        serviceFnName: 'create',
        description: 'executes create'
    },
    updateStatus: {
        serviceName,
        serviceFnName: 'updateStatus',
        description: 'executes updateStatus'
    },
    deleteSpecification: {
        serviceName,
        serviceFnName: 'deleteSpecification',
        description: 'executes deleteSpecification'
    }
};