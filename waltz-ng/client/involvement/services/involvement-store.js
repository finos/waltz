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

import {checkIsEntityInvolvementChangeCommand} from '../../common/checks';


function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/involvement`;


    const findAppsForEmployeeId = (employeeId) =>
        $http.get(`${BASE}/employee/${employeeId}/applications`)
            .then(result => result.data);


    const findEndUserAppsByIdSelector = (options) => $http
        .post(`${BASE}/end-user-application`, options)
        .then(r => r.data);


    const findChangeInitiativesForEmployeeId = (employeeId) =>
        $http.get(`${BASE}/employee/${employeeId}/change-initiative/direct`)
            .then(result => result.data);


    const findChangeInitiativesForEmployeeIdAndCIKind = (employeeId, kind) =>
        $http.get(`${BASE}/employee/${employeeId}/change-initiative/kind/${kind}/direct`)
            .then(result => result.data);


    const findByEmployeeId = (employeeId) =>
        $http.get(`${BASE}/employee/${employeeId}`)
            .then(result => result.data);


    const findByEntityReference = (kind, id) =>
        $http.get(`${BASE}/entity/${kind}/${id}`)
            .then(result => result.data);


    const findPeopleByEntityReference = (kind, id) =>
        $http.get(`${BASE}/entity/${kind}/${id}/people`)
            .then(result => result.data);


    const changeInvolvement = (entityRef, cmd) => {
        checkIsEntityInvolvementChangeCommand(cmd);
        return $http
            .post(`${BASE}/entity/${entityRef.kind}/${entityRef.id}`, cmd)
            .then(r => r.data);
    };


    return {
        findAppsForEmployeeId,
        findEndUserAppsByIdSelector,
        findByEmployeeId,
        findByEntityReference,
        findChangeInitiativesForEmployeeId,
        findChangeInitiativesForEmployeeIdAndCIKind,
        findPeopleByEntityReference,
        changeInvolvement
    };
}


store.$inject = ['$http', 'BaseApiUrl'];


const serviceName = 'InvolvementStore';


export const InvolvementStore_API = {
    findAppsForEmployeeId: {
        serviceName,
        serviceFnName: 'findAppsForEmployeeId',
        description: 'finds apps by employee id'
    },
    findEndUserAppsByIdSelector: {
        serviceName,
        serviceFnName: 'findEndUserAppsByIdSelector',
        description: 'finds end user apps by app id selector'
    },
    findByEmployeeId: {
        serviceName,
        serviceFnName: 'findByEmployeeId',
        description: 'find involvements by employee id'
    },
    findByEntityReference: {
        serviceName,
        serviceFnName: 'findByEntityReference',
        description: 'find involvements by entity reference'
    },
    findChangeInitiativesForEmployeeId: {
        serviceName,
        serviceFnName: 'findChangeInitiativesForEmployeeId',
        description: 'find change initiatives by employee id'
    },
    findChangeInitiativesForEmployeeIdAndCIKind: {
        serviceName,
        serviceFnName: 'findChangeInitiativesForEmployeeIdAndCIKind',
        description: 'find change initiatives by employee id'
    },
    findPeopleByEntityReference: {
        serviceName,
        serviceFnName: 'findPeopleByEntityReference',
        description: 'find people by involved entity reference'
    },
    changeInvolvement: {
        serviceName,
        serviceFnName: 'changeInvolvement',
        description: 'change person involvement for a given entity reference'
    }
};


export default {
    store,
    serviceName
};
