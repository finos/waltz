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


export function store($http, BaseApiUrl) {
    const BASE = `${BaseApiUrl}/entity-enum`;

    // --- FINDERS ---
    const findDefinitionsByEntityKind = (entityKind) => $http
        .get(`${BASE}/definition/${entityKind}`)
        .then(r => r.data);

    const findValuesByEntity = (entityRef) => {
        checkIsEntityRef(entityRef);
        return $http
            .get(`${BASE}/value/${entityRef.kind}/${entityRef.id}`)
            .then(r => r.data);
    };

    return {
        findDefinitionsByEntityKind,
        findValuesByEntity
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = 'EntityEnumStore';


export const EntityEnumStore_API = {
    findDefinitionsByEntityKind: {
        serviceName,
        serviceFnName: 'findDefinitionsByEntityKind',
        description: 'find all entity enum definitions given an entity kind'
    },
    findValuesByEntity: {
        serviceName,
        serviceFnName: 'findValuesByEntity',
        description: 'find all entity enum values given an entity reference'
    }
};


