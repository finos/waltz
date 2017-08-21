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

import {checkIsEntityRef} from "../../common/checks";

export function store($http, baseApiUrl) {
    const base = `${baseApiUrl}/attestation-instance`;

    const attestInstance = (id) => {
        return $http
            .post(`${base}/attest/${id}`);
    };

    const findByRunId = (id) => {
        return $http
            .get(`${base}/run/${id}`)
            .then(result => result.data);
    };

    const findByUser = () => {
        return $http
            .get(`${base}/user`)
            .then(result => result.data);
    };

    const findPersonsById = (id) => {
        return $http
            .get(`${base}/${id}/person`)
            .then(result => result.data);
    };

    const findByEntityRef = (ref) => {
        checkIsEntityRef(ref);

        return $http
            .get(`${base}/entity/${ref.kind}/${ref.id}`)
            .then(r => r.data);
    };
    
    return {
        attestInstance,
        findByRunId,
        findByUser,
        findPersonsById,
        findByEntityRef
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = 'AttestationInstanceStore';


export const AttestationInstanceStore_API = {
    attestInstance: {
        serviceName,
        serviceFnName: 'attestInstance',
        description: 'create an attestation'
    },
    findByRunId: {
        serviceName,
        serviceFnName: 'findByRunId',
        description: 'find attestations by a run id'
    },
    findByUser: {
        serviceName,
        serviceFnName: 'findByUser',
        description: 'find attestations for a user'
    },
    findPersonsById: {
        serviceName,
        serviceFnName: 'findPersonsById',
        description: 'find recipients (person) for an instance'
    },
    findByEntityRef: {
        serviceName,
        serviceFnName: 'findByEntityRef',
        description: 'find instances for an entity'
    }
};