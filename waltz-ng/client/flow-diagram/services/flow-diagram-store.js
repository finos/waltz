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

export function store($http, base) {
    const BASE = `${base}/flow-diagram`;

    const getById = (id) => $http
        .get(`${BASE}/id/${id}`)
        .then(r => r.data);

    const deleteForId = (id) => $http
        .delete(`${BASE}/id/${id}`)
        .then(r => r.data);

    const findByEntityReference = (ref) => $http
        .get(`${BASE}/entity/${ref.kind}/${ref.id}`)
        .then(r => r.data);

    const findForSelector = (options) => $http
        .post(`${BASE}/selector`, options)
        .then(r => r.data);

    // diagram -> diagramId
    const save = (diagram) => $http
        .post(BASE, diagram)
        .then(r => r.data);

    return {
        getById,
        deleteForId,
        findByEntityReference,
        findForSelector,
        save
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];




export const serviceName = 'FlowDiagramStore';


export const FlowDiagramStore_API = {
    getById: {
        serviceName,
        serviceFnName: 'getById',
        description: 'getById'
    },
    deleteForId: {
        serviceName,
        serviceFnName: 'deleteForId',
        description: 'deleteForId'
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
    save: {
        serviceName,
        serviceFnName: 'save',
        description: 'save'
    }
};