
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

export function store($http, baseUrl) {

    const BASE = `${baseUrl}/end-user-application`;

    const findBySelector = (selector) =>
        $http
            .post(`${BASE}/selector`, selector)
            .then(result => result.data);

    const countByOrganisationalUnit = () => $http
        .get(`${BASE}/count-by/org-unit`)
        .then(result => result.data);

    return {
        findBySelector,
        countByOrganisationalUnit
    };
}

store.$inject = ['$http', 'BaseApiUrl'];


export const serviceName = 'EndUserAppStore';


export const EndUserAppStore_API = {
    findBySelector: {
        serviceName,
        serviceFnName: 'findBySelector',
        description: 'find EUC apps by org unit selector'
    },
    countByOrganisationalUnit: {
        serviceName,
        serviceFnName: 'countByOrganisationalUnit',
        description: 'count EUC apps across all OUs'
    }
};

