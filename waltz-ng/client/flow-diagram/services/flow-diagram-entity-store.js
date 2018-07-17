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

export function store($http, base) {
    const BASE = `${base}/flow-diagram-entity`;

    const findByDiagramId = (id) => $http
        .get(`${BASE}/id/${id}`)
        .then(r => r.data);

    const findByEntityReference = (ref) => $http
        .get(`${BASE}/entity/${ref.kind}/${ref.id}`)
        .then(r => r.data);

    const findForSelector = (options) => $http
        .post(`${BASE}/selector`, options)
        .then(r => r.data);

    const addRelationship = (diagramId, ref) => {
        checkIsEntityRef(ref);
        return $http
            .post(`${BASE}/id/${diagramId}/${ref.kind}/${ref.id}`, {})
            .then(r => r.data);
    };

    const removeRelationship = (diagramId, ref) => {
        checkIsEntityRef(ref);
        return $http
            .delete(`${BASE}/id/${diagramId}/${ref.kind}/${ref.id}`)
            .then(r => r.data);
    };


    return {
        findByDiagramId,
        findByEntityReference,
        findForSelector,
        addRelationship,
        removeRelationship
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


const serviceName = 'FlowDiagramEntityStore';


export default {
    serviceName,
    store
};


export const FlowDiagramEntityStore_API = {
    findByDiagramId: {
        serviceName,
        serviceFnName: 'findByDiagramId',
        description: 'findByDiagramId'
    },
    findByEntityReference: {
        serviceName,
        serviceFnName: 'findByEntityReference',
        description: 'findByEntityReference'
    },
    findForSelector: {
        serviceName,
        serviceFnName: 'findForSelector',
        description: 'findForSelector'
    },
    addRelationship: {
        serviceName,
        serviceFnName: 'addRelationship',
        description: 'addRelationship (diagramId, ref)'
    },
    removeRelationship: {
        serviceName,
        serviceFnName: 'removeRelationship',
        description: 'removeRelationship (diagramId, ref)'
    }
};