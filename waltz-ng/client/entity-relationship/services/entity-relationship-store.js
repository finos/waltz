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

export function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/entity-relationship`;


    const remove = (rel) => {
        const url = `${BASE}/relationship/${rel.a.kind}/${rel.a.id}/${rel.b.kind}/${rel.b.id}/${rel.relationship}`;
        return $http
            .delete(url);
    };


    const create = (rel) => {
        const url = `${BASE}/relationship/${rel.a.kind}/${rel.a.id}/${rel.b.kind}/${rel.b.id}/${rel.relationship}`;
        return $http
            .post(url, {});
    };


    /** this is a 'find' not a 'get' as 'undirected' may return two hits */
    const findBetweenEntities = (a,
                                 b,
                                 directed = true, // true == directed | false == undirected
                                 relationshipKinds = [] /*optional*/) => {
        const url = `${BASE}/relationship/${a.kind}/${a.id}/${b.kind}/${b.id}`;

        const params = {
            directed,
            relationshipKind: relationshipKinds
        };

        return $http
            .get(url, { params})
            .then(result => result.data);
    };


    const findForEntity = (ref,
                           directionality = "ANY",  // ANY | SOURCE | TARGET
                           relationshipKinds = []) => {

        const url = `${BASE}/entity/${ref.kind}/${ref.id}`;

        const params = {
            directionality,
            relationshipKind: relationshipKinds
        };

        return $http
            .get(url, { params})
            .then(result => result.data);
    };

    return {
        findBetweenEntities,
        findForEntity,
        remove,
        create
    };

}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


export const serviceName = "EntityRelationshipStore";


export const EntityRelationshipStore_API = {
    findBetweenEntities: {
        serviceName,
        serviceFnName: "findBetweenEntities",
        description: "executes findBetweenEntities [ref, directed? (T|f), List?<RelationshipKinds>]"
    },
    findForEntity: {
        serviceName,
        serviceFnName: "findForEntity",
        description: "executes findForEntity [ref, directionality? (_ANY_|SOURCE|TARGET), List?<RelationshipKinds>]"
    },
    remove: {
        serviceName,
        serviceFnName: "remove",
        description: "executes remove [rel]"
    },
    create: {
        serviceName,
        serviceFnName: "create",
        description: "executes create [rel]"
    },
};
