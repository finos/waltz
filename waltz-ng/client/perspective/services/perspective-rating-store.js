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
import _ from "lodash";


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
        `${BASE}/entity/${ref.kind}/${ref.id}/${categoryX}/${categoryY}`;


    const findForEntity = (ref) => {

        return $http
            .get(`${BASE}/entity/${ref.kind}/${ref.id}`)
            .then(result => result.data);
    };


    const findForEntityAxis = (categoryX,
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


    const updateForEntityAxis = (categoryX,
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
        findForEntityAxis,
        updateForEntityAxis
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


const serviceName = 'PerspectiveRatingStore';


export const PerspectiveRatingStore_API = {
    findForEntity: {
        serviceName,
        serviceFnName: 'findForEntity',
        description: 'find perspective ratings for an entity'
    },
    findForEntityAxis: {
        serviceName,
        serviceFnName: 'findForEntityAxis',
        description: 'find ratings for a pair of categories'
    },
    updateForEntityAxis: {
        serviceName,
        serviceFnName: 'updateForEntityAxis',
        description: 'update ratings for a pair of categories'
    }
};


export default {
    serviceName,
    store
};