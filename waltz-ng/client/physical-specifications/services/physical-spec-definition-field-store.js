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


export function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/physical-spec-definition-field`;

    const findForSpecDefinitionId = (specDefId) => {
        return $http
            .get(`${base}/spec-definition/${specDefId}`)
            .then(r => r.data);
    };

    const createFields = (specDefId, commands) => {
        return $http
            .post(`${base}/spec-definition/${specDefId}/fields`, commands)
            .then(r => r.data);
    };

    return {
        findForSpecDefinitionId,
        createFields
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];



export const serviceName = 'PhysicalSpecDefinitionFieldStore';


export const PhysicalSpecDefinitionFieldStore_API = {
    findForSpecDefinitionId: {
        serviceName,
        serviceFnName: 'findForSpecDefinitionId',
        description: 'executes findForSpecDefinitionId'
    },
    createFields: {
        serviceName,
        serviceFnName: 'createFields',
        description: 'executes createFields'
    }
};