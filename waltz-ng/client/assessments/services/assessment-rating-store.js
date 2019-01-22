/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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


import { checkIsEntityRef } from '../../common/checks';

export function store($http, BaseApiUrl) {
    const BASE = `${BaseApiUrl}/assessment-rating`;


    const findForEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${BASE}/entity/${ref.kind}/${ref.id}`)
            .then(d => d.data);
    };


    const create = (ref, assessmentDefinitionId, ratingId, description = null) => {
        checkIsEntityRef(ref);
        return $http
            .post(`${BASE}/entity/${ref.kind}/${ref.id}/${assessmentDefinitionId}`, { ratingId, description })
            .then(d => d.data);
    };

    const update = (ref, assessmentDefinitionId, ratingId, description = null) => {
        checkIsEntityRef(ref);
        return $http
            .put(`${BASE}/entity/${ref.kind}/${ref.id}/${assessmentDefinitionId}`, { ratingId, description })
            .then(d => d.data);
    };

    const remove = (ref, assessmentDefinitionId) => {
        checkIsEntityRef(ref);
        return $http
            .delete(`${BASE}/entity/${ref.kind}/${ref.id}/${assessmentDefinitionId}`)
            .then(d => d.data);
    };


    return {
        findForEntityReference,
        create,
        update,
        remove
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl',
];


export const serviceName = 'AssessmentRatingStore';


export const AssessmentRatingStore_API = {
    findForEntityReference: {
        serviceName,
        serviceFnName: 'findForEntityReference',
        description: 'find all assessment ratings for an entity'
    },
    create: {
        serviceName,
        serviceFnName: "create",
        description: "create a rating"
    },
    update: {
        serviceName,
        serviceFnName: "update",
        description: "update a rating"
    },
    remove: {
        serviceName,
        serviceFnName: "remove",
        description: "remove a rating"
    }
};

