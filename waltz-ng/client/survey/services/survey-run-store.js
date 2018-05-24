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

    const createSurveyInstances = (surveyRunId, personIds = []) => {
        return $http
            .post(`${base}/${surveyRunId}/create-instances`, personIds)
            .then(result => result.data);
    };


    return {
        create,
        getById,
        findByTemplateId,
        findByEntityReference,
        findForUser,
        update,
        updateStatus,
        updateDueDate,
        generateSurveyRunRecipients,
        createSurveyRunInstancesAndRecipients,
        getCompletionRate,
        createSurveyInstances
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
    findForUser: {
        serviceName,
        serviceFnName: 'findForUser',
        description: 'find survey runs for a given user'
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
