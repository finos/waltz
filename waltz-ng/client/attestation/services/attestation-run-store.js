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

    const base = `${baseApiUrl}/attestation-run`;

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
    
    const findByRecipient = () => {
        return $http
            .get(`${base}/user`)
            .then(r => r.data);
    };

    return {
        create,
        getById,
        findByRecipient
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


const serviceName = 'AttestationRunStore';


export const AttestationRunStore_API = {
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
    findByRecipient: {
        serviceName,
        serviceFnName: 'findByRecipient',
        description: 'attestation runs for recipient'
    }
};


export default {
    serviceName,
    store
};
