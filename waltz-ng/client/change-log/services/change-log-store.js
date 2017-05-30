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
import _ from "lodash";


function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/change-log`;

    const findByEntityReference = (kind, id, limit = 30) => {
        let ref = null;
        if (_.isObject(kind)) {
            ref = kind;
            limit = id || 30;
        } else {
            console.log('DEPRECATED:change-log-store: use entity-ref not kind and id (#1290)');
            ref = {kind, id};
        }

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
        description: 'finds change log entries for a given entity reference'
    },
    findForUserName: {
        serviceName,
        serviceFnName: 'findForUserName',
        description: 'finds change log entries for a given user name'
    }
};


export default {
    store,
    serviceName
};

