
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

    const findByGroups = (groups = []) =>
        $http
            .get(`${base}/svg-diagram/group`, { params : { group: groups } })
            .then(r => r.data);

    const findByGroup = (group) => findByGroups([group]);

    return {
        findByGroup,
        findByGroups
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = 'SvgDiagramStore';


export const SvgDiagramStore_API = {
    findByGroup: {
        serviceName,
        serviceFnName: 'findByGroup',
        description: 'executes findByGroup'
    },
    findByGroups: {
        serviceName,
        serviceFnName: 'findByGroups',
        description: 'executes findByGroups'
    }
};
