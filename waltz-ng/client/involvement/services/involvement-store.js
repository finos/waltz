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

import _ from 'lodash';
import {checkIsEntityInvolvementChangeCommand} from '../../common/checks';


function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/involvement`;


    const findAppsForEmployeeId = (employeeId) =>
        $http.get(`${BASE}/employee/${employeeId}/applications`)
            .then(result => result.data);


    const findEndUserAppsByIdSelector = (options) => $http
        .post(`${BASE}/end-user-application`, options)
        .then(r => r.data);


    const findByEmployeeId = (employeeId) =>
        $http.get(`${BASE}/employee/${employeeId}`)
            .then(result => result.data);


    const findByEntityReference = (kind, id) => {
        const ref = _.isObject(kind) ? kind : { id, kind };

        return $http.get(`${BASE}/entity/${ref.kind}/${ref.id}`)
            .then(result => result.data);
    };


    const findPeopleByEntityReference = (kind, id) => {
        const ref = _.isObject(kind) ? kind : { id, kind };

        return $http.get(`${BASE}/entity/${ref.kind}/${ref.id}/people`)
            .then(result => result.data);
    };


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
