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

    const generateSurveyRunRecipients = (id) => {
        return $http
            .get(`${base}/${id}/recipients`)
    };

    const createSurveyRunInstancesAndRecipients = (id, excludedRecipients) => {
        return $http
            .post(`${base}/${id}/recipients`, excludedRecipients);
    };

    return {
        create,
        getById,
        findByTemplateId,
        findByEntityReference,
        findForUser,
        update,
        updateStatus,
        generateSurveyRunRecipients,
        createSurveyRunInstancesAndRecipients
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export default store;