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

    const BASE = `${BaseApiUrl}/entity-svg-diagram`;

    const findByEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http.get(`${BASE}/entity-ref/${ref.kind}/${ref.id}`)
            .then(result => result.data);
    };

    return {
        findByEntityReference,
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = 'EntitySvgDiagramStore';


export const EntitySvgDiagramStore_API = {
    findByEntityReference: {
        serviceName,
        serviceFnName: 'findByEntityReference',
        description: 'finds entity svg diagrams by entity reference'
    }
};