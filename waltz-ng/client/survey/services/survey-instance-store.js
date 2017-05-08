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

    const addRecipient = (id, command) => {
        return $http
            .post(`${base}/${id}/recipient`, command)
            .then(result => result.data);
    };

    const deleteRecipient = (id, instanceRecipientId) => {
        return $http
            .delete(`${base}/${id}/recipient/${instanceRecipientId}`,)
            .then(result => result.data);
    };

    return {
        getById,
        findByEntityReference,
        findForUser,
        findForSurveyRun,
        findRecipients,
        findResponses,
        saveResponse,
        updateStatus,
        updateDueDate,
        updateRecipient,
        addRecipient,
        deleteRecipient
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export default store;