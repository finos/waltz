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

function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/attestation-run`;

    const getCreateSummary = (cmd) => {
        return $http
            .post(`${base}/create-summary`, cmd)
            .then(r => r.data);
    };

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

    const findAll = () => {
        return $http
            .get(`${base}`)
            .then(r => r.data);
    };

    const findByRecipient = () => {
        return $http
            .get(`${base}/user`)
            .then(r => r.data);
    };

    const findResponseSummaries = () => {
        return $http
            .get(`${base}/summary/response`)
            .then(r => r.data);
    };

    const findByEntityRef = (ref) => {
        checkIsEntityRef(ref);

        return $http
            .get(`${base}/entity/${ref.kind}/${ref.id}`)
            .then(r => r.data);
    };

    return {
        getCreateSummary,
        create,
        getById,
        findAll,
        findByRecipient,
        findResponseSummaries,
        findByEntityRef
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


const serviceName = 'AttestationRunStore';


export const AttestationRunStore_API = {
    getCreateSummary: {
        serviceName,
        serviceFnName: 'getCreateSummary',
        description: 'get create summary when creating an attestation run'
    },
    create: {
        serviceName,
        serviceFnName: 'create',
        description: 'create an attestation run'
    },
    getById: {
        serviceName,
        serviceFnName: 'getById',
        description: 'attestation run by id'
    },
    findAll: {
        serviceName,
        serviceFnName: 'findAll',
        description: 'all attestation runs'
    },
    findByRecipient: {
        serviceName,
        serviceFnName: 'findByRecipient',
        description: 'attestation runs for recipient'
    },
    findResponseSummaries: {
        serviceName,
        serviceFnName: 'findResponseSummaries',
        description: 'attestation run response summaries'
    },
    findByEntityRef: {
        serviceName,
        serviceFnName: 'findByEntityRef',
        description: 'find runs for an entity'
    }
};


export default {
    serviceName,
    store
};
