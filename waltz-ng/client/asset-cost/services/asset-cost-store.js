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

const service = (http, root) => {

    const BASE = `${root}/asset-cost`;


    const findByCode = code =>
        http.get(`${BASE}/code/${code}`)
            .then(result => result.data);


    const findAppCostsByAppIds = (ids) =>
        http.post(`${BASE}/app-cost/apps`, { applicationIds: ids, year: 2015 })
            .then(result => result.data);


    const findStatsByAppIds = (ids) =>
        http.post(`${BASE}/app-cost/apps/stats`, { applicationIds: ids, year: 2015 })
            .then(result => result.data);


    return {
        findByCode,
        findAppCostsByAppIds,
        findStatsByAppIds
    };
};

service.$inject = ['$http', 'BaseApiUrl'];


export default service;