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
import {checkIsEntityRelationshipChangeCommand, checkIsIdSelector} from "../../common/checks";


function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/change-initiative`;


    const findBySelector = (selector) => {
        checkIsIdSelector(selector);
        return $http
            .post(`${BASE}/selector`, selector)
            .then(r => r.data);
    };


    const findHierarchyBySelector = (selector) => {
        checkIsIdSelector(selector);
        return $http
            .post(`${BASE}/hierarchy/selector`, selector)
            .then(r => r.data);
    };


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
        findBySelector,
        findHierarchyBySelector,
        findRelatedForId,
        getById,
        search,
        changeRelationship
    }
}

store.$inject = ['$http', 'BaseApiUrl'];


const serviceName = 'ChangeInitiativeStore';


export const ChangeInitiativeStore_API = {
    findBySelector: {
        serviceName,
        serviceFnName: 'findBySelector',
        description: 'finds change initiatives by id selector'
    },
    findHierarchyBySelector: {
        serviceName,
        serviceFnName: 'findHierarchyBySelector',
        description: 'finds change initiatives (and parents) by id selector'
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