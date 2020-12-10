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

import {checkIsEntityRef} from "../../common/checks";

function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/survey-instance`;

    const getById = (id) => {
        return $http
            .get(`${base}/id/${id}`)
            .then(result => result.data);
    };

    const findByEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${base}/entity/${ref.kind}/${ref.id}`)
            .then(r => r.data);
    };

    const findForRecipientId = (personId) => {
        return $http
            .get(`${base}/recipient/id/${personId}`)
            .then(r => r.data);
    };

    const findForUser = () => {
        return $http
            .get(`${base}/user`)
            .then(result => result.data);
    };

    const findForSurveyRun = (id) => {
        return $http
            .get(`${base}/run/${id}`)
            .then(result => result.data);
    };

    const findPreviousVersions = (originalId) => {
        return $http
            .get(`${base}/id/${originalId}/previous-versions`)
            .then(result => result.data);
    };

    const findRecipients = (id) => {
        return $http
            .get(`${base}/${id}/recipients`)
            .then(result => result.data);
    };

    const findResponses = (id) => {
        return $http
            .get(`${base}/${id}/responses`)
            .then(result => result.data);
    };

    const findPossibleActions = (id) => {
        return $http
            .get(`${base}/${id}/actions`)
            .then(result => result.data);
    };

    const getPermissions = (id) => {
        return $http
            .get(`${base}/${id}/permissions`)
            .then(result => result.data);
    };

    const saveResponse = (id, questionResponse) => {
        return $http
            .put(`${base}/${id}/response`, questionResponse)
            .then(result => result.data);
    };

    const updateStatus = (id, command) => {
        return $http
            .put(`${base}/${id}/status`, command)
            .then(result => result.data);
    };

    const updateDueDate = (id, command) => {
        return $http
            .put(`${base}/${id}/due-date`, command)
            .then(r => r.data);
    };

    const updateRecipient = (id, command) => {
        return $http
            .put(`${base}/${id}/recipient`, command)
            .then(result => result.data);
    };

    const markApproved = (id, reasonCommand) => {
        return $http
            .put(`${base}/${id}/approval`, reasonCommand)
            .then(result => result.data);
    };

    const addRecipient = (surveyInstanceId, command) => {
        return $http
            .post(`${base}/${surveyInstanceId}/recipient`, command)
            .then(result => result.data);
    };

    const deleteRecipient = (id, instanceRecipientId) => {
        return $http
            .delete(`${base}/${id}/recipient/${instanceRecipientId}`)
            .then(result => result.data);
    };

    return {
        getById,
        getPermissions,
        findByEntityReference,
        findForRecipientId,
        findForUser,
        findForSurveyRun,
        findPreviousVersions,
        findRecipients,
        findResponses,
        findPossibleActions,
        saveResponse,
        updateStatus,
        updateDueDate,
        updateRecipient,
        addRecipient,
        deleteRecipient,
        markApproved
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


const serviceName = 'SurveyInstanceStore';


export const SurveyInstanceStore_API = {
    getById: {
        serviceName,
        serviceFnName: 'getById',
        description: 'get survey instance for a given id'
    },
    findByEntityReference: {
        serviceName,
        serviceFnName: 'findByEntityReference',
        description: 'finds survey instances for a given entity reference'
    },
    findForRecipientId: {
        serviceName,
        serviceFnName: 'findForRecipientId',
        description: 'finds survey instances for a recipient person id'
    },
    findForUser: {
        serviceName,
        serviceFnName: 'findForUser',
        description: 'finds survey instances for the current logged in user'
    },
    findForSurveyRun: {
        serviceName,
        serviceFnName: 'findForSurveyRun',
        description: 'finds survey instances for a given survey run id'
    },
    findRecipients: {
        serviceName,
        serviceFnName: 'findRecipients',
        description: 'finds recipients for a given survey instance id'
    },
    findResponses: {
        serviceName,
        serviceFnName: 'findResponses',
        description: 'finds responses for a given survey instance id'
    },
    findPreviousVersions: {
        serviceName,
        serviceFnName: 'findPreviousVersions',
        description: 'finds previouse versions for a given survey instance id'
    },
    findPossibleActions: {
        serviceName,
        serviceFnName: 'findPossibleActions',
        description: 'finds all possible action on this survey instance'
    },
    getPermissions: {
        serviceName,
        serviceFnName: 'getPermissions',
        description: 'get permissions for this survey instance'
    },
    saveResponse: {
        serviceName,
        serviceFnName: 'saveResponse',
        description: 'save response for a given survey instance question'
    },
    updateStatus: {
        serviceName,
        serviceFnName: 'updateStatus',
        description: 'update status for a given survey instance id'
    },
    updateDueDate: {
        serviceName,
        serviceFnName: 'updateDueDate',
        description: 'update due date for a given survey instance id'
    },
    updateRecipient: {
        serviceName,
        serviceFnName: 'updateRecipient',
        description: 'update recipient for a given survey instance id'
    },
    markApproved: {
        serviceName,
        serviceFnName: 'markApproved',
        description: 'approve a survey instance response'
    },
    addRecipient: {
        serviceName,
        serviceFnName: 'addRecipient',
        description: 'add recipient to a given survey instance id'
    },
    deleteRecipient: {
        serviceName,
        serviceFnName: 'deleteRecipient',
        description: 'delete recipient from a given survey instance id'
    }
};


export default {
    store,
    serviceName
};
