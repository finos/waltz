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
import {checkIsEntityRelationshipChangeCommand} from "../../common/checks";


function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/change-initiative`;


    const findByRef = (kind, id) => $http
            .get(`${BASE}/ref/${kind}/${id}`)
            .then(r => r.data);


    const findByParentId = (id) => $http
        .get(`${BASE}/children/${id}`)
        .then(r => r.data);


    const getById = (id) => $http
            .get(`${BASE}/id/${id}`)
            .then(r => r.data);


    const findRelatedForId = (id) => $http
            .get(`${BASE}/id/${id}/related`)
            .then(r => r.data);


    const search = (query) => $http
            .get(`${BASE}/search/${query}`)
            .then(r => r.data);


    const changeRelationship = (id, cmd) => {
        checkIsEntityRelationshipChangeCommand(cmd);
        return $http
            .post(`${BASE}/id/${id}/entity-relationship`, cmd)
            .then(r => r.data);
    };


    return {
        findByParentId,
        findByRef,
        findRelatedForId,
        getById,
        search,
        changeRelationship
    }
}

store.$inject = ['$http', 'BaseApiUrl'];


const serviceName = 'ChangeInitiativeStore';


export const ChangeInitiativeStore_API = {
    findByParentId: {
        serviceName,
        serviceFnName: 'findByParentId',
        description: 'finds change initiatives by parent id'
    },
    findByRef: {
        serviceName,
        serviceFnName: 'findByRef',
        description: 'finds change initiatives by an entity reference'
    },
    getById: {
        serviceName,
        serviceFnName: 'getById',
        description: 'get a change initiative by id'
    },
    findRelatedForId: {
        serviceName,
        serviceFnName: 'findRelatedForId',
        description: 'find related change initiatives related to one with supplied id'
    },
    search: {
        serviceName,
        serviceFnName: 'search',
        description: 'search change initiatives'
    },
    changeRelationship: {
        serviceName,
        serviceFnName: 'changeRelationship',
        description: 'change relationship between a change initiative and entity reference'
    }
};


export default {
    store,
    serviceName
};