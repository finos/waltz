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


function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/change-log`;

    const findByEntityReference = (ref, limit = 30) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${BASE}/${ref.kind}/${ref.id}`, {params: {limit}})
            .then(result => result.data);
    };

    const findForUserName = (userName, limit = null) =>
        $http.get(`${BASE}/user/${userName}`, {params: {limit}})
            .then(r => r.data);

    return {
        findByEntityReference,
        findForUserName
    };
}

store.$inject = [
    '$http',
    'BaseApiUrl'
];


const serviceName = 'ChangeLogStore';


export const ChangeLogStore_API = {
    findByEntityReference: {
        serviceName,
        serviceFnName: 'findByEntityReference',
        description: 'finds change log entries for a given entity reference and limit (default: 30)'
    },
    findForUserName: {
        serviceName,
        serviceFnName: 'findForUserName',
        description: 'finds change log entries for a given user name and limit (default: no limit)'
    }
};


export default {
    store,
    serviceName
};

