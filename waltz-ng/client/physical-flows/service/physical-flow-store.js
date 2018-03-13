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

import {checkIsEntityRef, checkIsIdSelector, checkIsCreatePhysicalFlowCommand} from "../../common/checks";


export function store($http, baseApiUrl) {

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


    const findByLogicalFlowId = (id) => {
        return $http
            .get(`${base}/logical-flow/${id}`)
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


    const updateSpecDefinitionId = (flowId, command) => {
        return $http
            .post(`${base}/id/${flowId}/spec-definition`, command)
            .then(r => r.data);
    };


    const updateAttribute = (flowId, command) => {
        return $http
            .post(`${base}/id/${flowId}/attribute`, command)
            .then(r => r.data);
    };


    const validateUpload = (commands) => {
        return $http
            .post(`${base}/upload/validate`, commands)
            .then(r => r.data);
    };


    const upload = (commands) => {
        return $http
            .post(`${base}/upload`, commands)
            .then(r => r.data);
    };


    const cleanupOrphans = () => $http
        .get(`${base}/cleanup-orphans`)
        .then(r => r.data);


    return {
        findBySpecificationId,
        findByLogicalFlowId,
        findByEntityReference,
        findByProducerEntityReference,
        findByConsumerEntityReference,
        findBySelector,
        getById,
        searchReports,
        create,
        deleteById,
        updateSpecDefinitionId,
        updateAttribute,
        validateUpload,
        upload,
        cleanupOrphans
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = 'PhysicalFlowStore';



export const PhysicalFlowStore_API = {
    findBySpecificationId: {
        serviceName,
        serviceFnName: 'findBySpecificationId',
        description: 'executes findBySpecificationId'
    },
    findByLogicalFlowId: {
        serviceName,
        serviceFnName: 'findByLogicalFlowId',
        description: 'executes findByLogicalFlowId'
    },
    findByEntityReference: {
        serviceName,
        serviceFnName: 'findByEntityReference',
        description: 'executes findByEntityReference'
    },
    findByProducerEntityReference: {
        serviceName,
        serviceFnName: 'findByProducerEntityReference',
        description: 'executes findByProducerEntityReference'
    },
    findByConsumerEntityReference: {
        serviceName,
        serviceFnName: 'findByConsumerEntityReference',
        description: 'executes findByConsumerEntityReference'
    },
    findBySelector: {
        serviceName,
        serviceFnName: 'findBySelector',
        description: 'executes findBySelector'
    },
    getById: {
        serviceName,
        serviceFnName: 'getById',
        description: 'executes getById'
    },
    searchReports: {
        serviceName,
        serviceFnName: 'searchReports',
        description: 'executes searchReports'
    },
    create: {
        serviceName,
        serviceFnName: 'create',
        description: 'executes create'
    },
    deleteById: {
        serviceName,
        serviceFnName: 'deleteById',
        description: 'executes deleteById'
    },
    updateSpecDefinitionId: {
        serviceName,
        serviceFnName: 'updateSpecDefinitionId',
        description: 'executes updateSpecDefinitionId'
    },
    updateAttribute: {
        serviceName,
        serviceFnName: 'updateAttribute',
        description: 'executes updateAttribute'
    },
    validateUpload: {
        serviceName,
        serviceFnName: 'validateUpload',
        description: 'executes validateUpload'
    },
    upload: {
        serviceName,
        serviceFnName: 'upload',
        description: 'executes upload'
    },
    cleanupOrphans: {
        serviceName,
        serviceFnName: 'cleanupOrphans',
        description: 'cleans up orphaned physical flows'
    }
};