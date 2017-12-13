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

export function store($http, base) {
    const baseUrl = `${base}/static-panel`;

    const findByGroups = (groups = []) => {
        return $http
            .get(`${baseUrl}/group`, {params: {group: groups}})
            .then(r => r.data);
    };

    const findAll = () =>
        $http
            .get(baseUrl)
            .then(r => r.data);

    const findByGroup = (group) => findByGroups([group]);

    const save = (p) =>
        $http
            .post(baseUrl, p)
            .then(r => r.data);

    return {
        findAll,
        findByGroup,
        findByGroups,
        save
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];



export const serviceName = 'StaticPanelStore';


export const StaticPanelStore_API = {
    findAll: {
        serviceName,
        serviceFnName: 'findAll',
        description: 'findAll'
    },
    findByGroup: {
        serviceName,
        serviceFnName: 'findByGroup',
        description: 'findByGroup'
    },
    findByGroups: {
        serviceName,
        serviceFnName: 'findByGroups',
        description: 'findByGroups'
    },
    save: {
        serviceName,
        serviceFnName: 'save',
        description: 'save'
    }
};