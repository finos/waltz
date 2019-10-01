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


export function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/external-identifier`;

    const findByEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${base}/entity/${ref.kind}/${ref.id}`)
            .then(r => r.data);
    };

    const deleteExternalId = (ref, id, system) => $http
            .delete(`${base}/entity/${ref.kind}/${ref.id}/externalId/${id}/${system}`)
        .then(r => r.data);

    const addExternalIdentifier = (ref, id) => $http
        .post(`${base}/entity/${ref.kind}/${ref.id}/externalId/${id}`)
        .then(r => r.data);

    return {
        findByEntityReference,
        addExternalIdentifier,
        deleteExternalId
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


export const serviceName = "ExternalIdentifierStore";



export const ExternalIdentifierStore_API = {
    findByEntityReference: {
        serviceName,
        serviceFnName: "findByEntityReference",
        description: "executes findByEntityReference"
    },
    addExternalIdentifier: {
        serviceName,
        serviceFnName: "addExternalIdentifier",
        description: "executes addExternalIdentifier"
    },
    deleteExternalId: {
        serviceName,
        serviceFnName: "deleteExternalId",
        description: "executes deleteExternalId"
    }
};