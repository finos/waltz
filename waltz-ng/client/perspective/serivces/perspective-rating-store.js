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

import _ from 'lodash';


function stripRatings(ratings = []) {
    return _.map(
        ratings,
        r => ({
            measurableX: r.measurableX,
            measurableY: r.measurableY,
            rating: r.rating
        }));
}


function store($http,
               BaseApiUrl) {
    const BASE = `${BaseApiUrl}/perspective-rating`;


    const mkEntityUrl = (categoryX, categoryY, ref) =>
        `${BASE}/${categoryX}/${categoryY}/entity/${ref.kind}/${ref.id}`;

    const findForEntity = (categoryX,
                           categoryY,
                           ref) => {
        const url = mkEntityUrl(
            categoryX,
            categoryY,
            ref);
        return $http
            .get(url)
            .then(result => result.data);
    };

    const updateForEntity = (categoryX,
                             categoryY,
                             ref,
                             perspectiveRatings = []) => {
        const url = mkEntityUrl(
            categoryX,
            categoryY,
            ref);
        const strippedRatings = stripRatings(perspectiveRatings);
        return $http
            .put(url, strippedRatings)
            .then(result => result.data);
    };

    return {
        findForEntity,
        updateForEntity
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export default store;
