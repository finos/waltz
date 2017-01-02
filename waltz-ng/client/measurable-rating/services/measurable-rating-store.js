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
import { checkIsEntityRef, checkIsIdSelector } from '../../common/checks';


function store($http, baseApiUrl) {

    const baseUrl = `${baseApiUrl}/measurable-rating`;

    const findForEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${baseUrl}/entity/${ref.kind}/${ref.id}`)
            .then(d => d.data);
    };

    const findByMeasurableSelector = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${baseUrl}/measurable-selector`, options)
            .then(d => d.data);
    };

    const countByMeasurable = () => {
        return $http
            .get(`${baseUrl}/count-by/measurable`)
            .then(d => d.data);
    };

    const create = (ref, measurableId, rating = 'Z', description = '') => {
        checkIsEntityRef(ref);
        return $http
            .post(`${baseUrl}/entity/${ref.kind}/${ref.id}/${measurableId}`, { rating, description })
            .then(d => d.data);
    };

    const update = (ref, measurableId, rating = 'Z', description = '') => {
        checkIsEntityRef(ref);
        return $http
            .put(`${baseUrl}/entity/${ref.kind}/${ref.id}/${measurableId}`, { rating, description })
            .then(d => d.data);
    };

    const remove = (ref, measurableId) => {
        checkIsEntityRef(ref);
        return $http
            .delete(`${baseUrl}/entity/${ref.kind}/${ref.id}/${measurableId}`)
            .then(d => d.data);
    };

    return {
        findByMeasurableSelector,
        findForEntityReference,
        countByMeasurable,
        create,
        update,
        remove
    };

}

store.$inject = ['$http', 'BaseApiUrl'];

export default store;
