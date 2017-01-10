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

import {checkIsEntityRef, checkIsStringList} from "../../common/checks";


function service($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/entity/alias`;

    const update = (entityRef, aliases = []) => {
        checkIsEntityRef(entityRef);
        checkIsStringList(aliases);

        return $http
            .post(`${BASE}/${entityRef.kind}/${entityRef.id}`, aliases)
            .then(r => r.data);
    };

    const getForEntity = (entityRef) => {
        checkIsEntityRef(entityRef);

        return $http
            .get(`${BASE}/${entityRef.kind}/${entityRef.id}`)
            .then(r => r.data);
    };


    return {
        update,
        getForEntity
    }
}


service.$inject = [
    '$http',
    'BaseApiUrl'
];

export default service;