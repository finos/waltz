/*
 *  This file is part of Waltz.
 *
 *  Waltz is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Waltz is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

function service($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/app-group`;


    const getById = (id) => $http
        .get(`${BASE}/id/${id}`)
        .then(result => result.data);


    const findMyGroupSubscriptions = (id) => $http
        .get(`${BASE}/my-group-subscriptions`)
        .then(result => result.data);


    const findPublicGroups = (id) => $http
        .get(`${BASE}/public`)
        .then(result => result.data);


    const subscribe = (id) => $http
        .post(`${BASE}/id/${id}/subscribe`, {})
        .then(result => result.data);



    return {
        getById,
        findMyGroupSubscriptions,
        findPublicGroups,
        subscribe
    };

}

service.$inject = [
    '$http',
    'BaseApiUrl'
];


export default service;
