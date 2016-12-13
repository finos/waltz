/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import _ from "lodash";


function accessLogStore($http, BaseApiUrl) {
    const BASE = `${BaseApiUrl}/access-log`;

    const write = (state, params) => {
        const paramStr = _.isObject(params)
            ? JSON.stringify(params)
            : params;

        return $http
            .post(`${BASE}/${state}/${paramStr}`, null)
            .then(r => r.data);
    };


    const findActiveUsers = (minutes) => $http
        .get(`${BASE}/active/${minutes}`)
        .then(r => r.data);


    const findForUserName = (userName) => $http
        .get(`${BASE}/user/${userName}`)
        .then(r => r.data);


    return {
        findActiveUsers,
        findForUserName,
        write
    };
}


accessLogStore.$inject = [
    '$http',
    'BaseApiUrl'
];


export default accessLogStore;
