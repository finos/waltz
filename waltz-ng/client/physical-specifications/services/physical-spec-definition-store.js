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