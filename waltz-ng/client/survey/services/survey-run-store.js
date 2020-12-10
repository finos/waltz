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

function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/survey-run`;

    const create = (cmd) => {
        return $http
            .post(base, cmd)
            .then(r => r.data);
    };

    const deleteById = (id) => {
        return $http
            .delete(`${base}/${id}`)
            .then(r => r.data);
    };

    const getById = (id) => {
        return $http
            .get(`${base}/id/${id}`)
            .then(r => r.data);
    };

    const findByTemplateId = (id) => {
        return $http
            .get(`${base}/template-id/${id}`)
            .then(r => r.data);
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
            .then(r => r.data);
    };

    const update = (id, cmd) => {
        return $http
            .put(`${base}/${id}`, cmd)
            .then(r => r.data);
    };

    const updateStatus = (id, newStatus) => {
        return $http
            .put(`${base}/${id}/status`, newStatus)
            .then(r => r.data);
    };

    const updateDueDate = (id, command) => {
        return $http
            .put(`${base}/${id}/due-date`, command)
            .then(r => r.data);
    };

    const updateOwningRole = (id, command) => {
        return $http
            .put(`${base}/${id}/role`, command)
            .then(r => r.data);
    };

    const generateSurveyRunRecipients = (id) => {
        return $http
            .get(`${base}/${id}/recipients`)
    };

    const createSurveyRunInstancesAndRecipients = (id, excludedRecipients) => {
        return $http
            .post(`${base}/${id}/recipients`, excludedRecipients);
    };

    const getCompletionRate = (id) => {
        return $http
            .get(`${base}/${id}/completion-rate`)
            .then(r => r.data);
    };

    const createSurveyInstances = (surveyRunId, peopleAndRoles) => {
        return $http
            .post(`${base}/${surveyRunId}/create-instances`, peopleAndRoles )
            .then(result => result.data);
    };


    return {
        create,
        deleteById,
        getById,
        findByTemplateId,
        findByEntityReference,
        findForRecipientId,
        findForUser,
        update,
        updateStatus,
        updateDueDate,
        generateSurveyRunRecipients,
        createSurveyRunInstancesAndRecipients,
        getCompletionRate,
        createSurveyInstances,
        updateOwningRole
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


const serviceName = 'SurveyRunStore';


export const SurveyRunStore_API = {
    create: {
        serviceName,
        serviceFnName: 'create',
        description: 'create survey run'
    },
    deleteById: {
        serviceName,
        serviceFnName: 'deleteById',
        description: 'delete survey run for a given id'
    },
    getById: {
        serviceName,
        serviceFnName: 'getById',
        description: 'get survey run for a given id'
    },
    findByTemplateId: {
        serviceName,
        serviceFnName: 'findByTemplateId',
        description: 'find survey runs for a given template id'
    },
    findByEntityReference: {
        serviceName,
        serviceFnName: 'findByEntityReference',
        description: 'find survey runs for a given entity'
    },
    findForRecipientId: {
        serviceName,
        serviceFnName: 'findForRecipientId',
        description: 'find survey runs for a given recipient person id'
    },
    findForUser: {
        serviceName,
        serviceFnName: 'findForUser',
        description: 'find survey runs for the current logged in user'
    },
    update: {
        serviceName,
        serviceFnName: 'update',
        description: 'update a survey run'
    },
    updateStatus: {
        serviceName,
        serviceFnName: 'updateStatus',
        description: `update a survey run's status`
    },
    updateDueDate: {
        serviceName,
        serviceFnName: 'updateDueDate',
        description: `update a survey run's due date`
    },
    updateOwningRole: {
        serviceName,
        serviceFnName: 'updateOwningRole',
        description: `update the owning role for all instances in a run`
    },
    generateSurveyRunRecipients: {
        serviceName,
        serviceFnName: 'generateSurveyRunRecipients',
        description: 'generate recipient for a given survey run id'
    },
    createSurveyRunInstancesAndRecipients: {
        serviceName,
        serviceFnName: 'createSurveyRunInstancesAndRecipients',
        description: 'create a survey run, instances and recipients'
    },
    getCompletionRate: {
        serviceName,
        serviceFnName: 'getCompletionRate',
        description: 'get completion rate for a given survey run id'
    },
    createSurveyInstances: {
        serviceName,
        serviceFnName: 'createSurveyInstances',
        description: 'create a survey instance for a given survey run and list of person ids'
    }
};


export default {
    store,
    serviceName
};
