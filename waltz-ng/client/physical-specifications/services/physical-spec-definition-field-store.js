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

    const base = `${baseApiUrl}/physical-spec-definition-field`;

    const findBySelector = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${base}/selector`, options)
            .then(r => r.data);
    };

    const findForSpecDefinitionId = (specDefId) => {
        return $http
            .get(`${base}/spec-definition/${specDefId}`)
            .then(r => r.data);
    };

    const createFields = (specDefId, commands) => {
        return $http
            .post(`${base}/spec-definition/${specDefId}/fields`, commands)
            .then(r => r.data);
    };

    const updateDescription = (id, command) => {
        return $http
            .put(`${base}/${id}/description`, command)
            .then(r => r.data);
    };

    const updateLogicalElement = (id, command) => {
        return $http
            .put(`${base}/${id}/logical-data-element`, command)
            .then(r => r.data);
    };

    return {
        findBySelector,
        findForSpecDefinitionId,
        createFields,
        updateDescription,
        updateLogicalElement
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = 'PhysicalSpecDefinitionFieldStore';


export const PhysicalSpecDefinitionFieldStore_API = {
    findBySelector: {
        serviceName,
        serviceFnName: 'findBySelector',
        description: 'executes findBySelector'
    },
    findForSpecDefinitionId: {
        serviceName,
        serviceFnName: 'findForSpecDefinitionId',
        description: 'executes findForSpecDefinitionId'
    },
    createFields: {
        serviceName,
        serviceFnName: 'createFields',
        description: 'executes createFields'
    },
    updateDescription: {
        serviceName,
        serviceFnName: 'updateDescription',
        description: 'executes updateDescription'
    },
    updateLogicalElement: {
        serviceName,
        serviceFnName: 'updateLogicalElement',
        description: 'executes updateLogicalElement'
    }
};