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


export default [
    '$http',
    'BaseApiUrl',
    ($http, BaseApiUrl) => {
        const BASE = `${BaseApiUrl}/capability`;


        const findAll = () =>
            $http.get(BASE)
                .then(result => result.data);


        const findByAppIds = (ids) =>
            $http.post(`${BASE}/apps`, ids)
                .then(result => result.data);


        const findByIds = (ids) =>
            $http.post(`${BASE}/ids`, ids)
                .then(result => result.data);


        const search = (query) => $http
            .get(`${BASE}/search/${query}`)
            .then(x => x.data);


        return {
            findAll,
            findByAppIds,
            findByIds,
            search
        };
    }
];
