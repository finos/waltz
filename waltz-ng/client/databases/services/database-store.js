
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

function service(http, baseUrl) {

    const BASE = `${baseUrl}/database`;

    const findByAppId = (appId) =>
        http.get(`${BASE}/app/${appId}`)
            .then(result => result.data);

    const findBySelector = (id, kind, scope='CHILDREN') =>
        http.post(`${BASE}`, { scope, entityReference: { id, kind }})
            .then(result => result.data);

    const findStatsForSelector = (id, kind, scope='CHILDREN') =>
        http.post(`${BASE}/stats`, { scope, entityReference: { id, kind }})
            .then(result => result.data);

    return {
        findByAppId,
        findBySelector,
        findStatsForSelector
    };
}

service.$inject = ['$http', 'BaseApiUrl'];


export default service;
