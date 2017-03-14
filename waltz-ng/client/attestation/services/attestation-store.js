
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

export default [
    '$http',
    'BaseApiUrl',
    ($http, BaseApiUrl) => {

        const BASE = `${BaseApiUrl}/attestation`;

        const findForEntity = (ref) => {
            return $http
                .get(`${BASE}/entity/${ref.kind}/${ref.id}`)
                .then(result => result.data);
        };


        const recalculateForLineage = () => {
            return $http
                .get(`${BASE}/calculate-all/lineage`)
                .then(result => result.data);
        };


        return {
            findForEntity,
            recalculateForLineage
        };
    }
];
