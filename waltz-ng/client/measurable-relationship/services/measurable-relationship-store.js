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
    const BASE = `${base}/measurable-relationship`;

    const findForMeasurable = (id) => $http
        .get(`${BASE}/measurable/${id}`)
        .then(r => r.data);

    const remove = (idA, idB) => $http
        .delete(`${BASE}/${idA}/${idB}`)
        .then(r => r.data);

    const save = (d) => $http
        .post(`${BASE}/${d.measurableA}/${d.measurableB}/${d.relationshipKind}`, d.description)
        .then(r => r.data);

    return {
        findForMeasurable,
        remove,
        save
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];





export const serviceName = 'MeasurableRelationshipStore';


export const MeasurableRelationshipStore_API = {
    findByMeasurable: {
        serviceName,
        serviceFnName: 'findForMeasurable',
        description: 'finds relationships by given measurable id'
    },
    save: {
        serviceName,
        serviceFnName: 'save',
        description: 'saves an entity named note'
    },
    remove: {
        serviceName,
        serviceFnName: 'remove',
        description: 'removes an entity named note'
    }
};