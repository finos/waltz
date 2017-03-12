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

function store($http, base) {
    const BASE = `${base}/entity-tag`;

    const findAllTags = () => $http
        .get(`${BASE}/tags`)
        .then(x => x.data);


    const findByTag = (tag) => $http
        .post(`${BASE}/tags`, tag)
        .then(x => x.data);

    const findTagsByEntityRef = (ref) => $http
        .post(`${BASE}/entity/${ref.kind}/${ref.id}`)
        .then(x => x.data);

    return {
        findAllTags,
        findByTag,
        findTagsByEntityRef
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export default store;
