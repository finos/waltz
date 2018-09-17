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
import {checkIsEntityRef, checkIsIdSelector} from "../../common/checks";


function store($http, baseApiUrl) {

    const baseUrl = `${baseApiUrl}/measurable-rating`;

    const findForEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${baseUrl}/entity/${ref.kind}/${ref.id}`)
            .then(d => d.data);
    };

    const findByCategory = (id) => {
        return $http
            .get(`${baseUrl}/category/${id}`)
            .then(d => d.data);
    };

    const findByMeasurableSelector = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${baseUrl}/measurable-selector`, options)
            .then(d => d.data);
    };

    const findByAppSelector = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${baseUrl}/app-selector`, options)
            .then(d => d.data);
    };

    const statsByAppSelector = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${baseUrl}/stats-by/app-selector`, options)
            .then(d => d.data);
    };

    const statsForRelatedMeasurables = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${baseUrl}/related-stats/measurable`, options)
            .then(d => d.data);
    };

    const countByMeasurable = () => {
        return $http
            .get(`${baseUrl}/count-by/measurable`)
            .then(d => d.data);
    };

    const countByMeasurableCategory = (id) => {
        return $http
            .get(`${baseUrl}/count-by/measurable/category/${id}`)
            .then(d => d.data);
    };

    const create = (ref, measurableId, rating = "Z", description = "", plannedDate = null) => {
        checkIsEntityRef(ref);
        return $http
            .post(`${baseUrl}/entity/${ref.kind}/${ref.id}/${measurableId}`, { rating, description, plannedDate })
            .then(d => d.data);
    };

    const update = (ref, measurableId, rating = "Z", description = "", plannedDate = null) => {
        checkIsEntityRef(ref);
        return $http
            .put(`${baseUrl}/entity/${ref.kind}/${ref.id}/${measurableId}`, { rating, description, plannedDate })
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
        findByAppSelector,
        findByCategory,
        findForEntityReference,
        countByMeasurable,
        countByMeasurableCategory,
        statsByAppSelector,
        statsForRelatedMeasurables,
        create,
        update,
        remove
    };

}

store.$inject = ["$http", "BaseApiUrl"];


const serviceName = "MeasurableRatingStore";


export const MeasurableRatingStore_API = {
    findByMeasurableSelector: {
        serviceName,
        serviceFnName: "findByMeasurableSelector",
        description: "finds measurables by measurable selector"
    },
    findByAppSelector: {
        serviceName,
        serviceFnName: "findByAppSelector",
        description: "finds measurables by app selector"
    },
    findByCategory: {
        serviceName,
        serviceFnName: "findByCategory",
        description: "finds measurable ratings for a given category id"
    },
    findForEntityReference: {
        serviceName,
        serviceFnName: "findForEntityReference",
        description: "find measurables for an entity reference"
    },
    countByMeasurable: {
        serviceName,
        serviceFnName: "countByMeasurable",
        description: "return a count by measurable"
    },
    countByMeasurableCategory: {
        serviceName,
        serviceFnName: "countByMeasurableCategory",
        description: "return a count by measurable category [categoryId]"
    },
    statsByAppSelector: {
        serviceName,
        serviceFnName: "statsByAppSelector",
        description: "return measurable stats by app selector"
    },
    statsForRelatedMeasurables: {
        serviceName,
        serviceFnName: "statsForRelatedMeasurables",
        description: "return stats for related measurables"
    },
    create: {
        serviceName,
        serviceFnName: "create",
        description: "create a measurable"
    },
    update: {
        serviceName,
        serviceFnName: "update",
        description: "update a measurable"
    },
    remove: {
        serviceName,
        serviceFnName: "remove",
        description: "remove a measurable"
    }

};


export default {
    serviceName,
    store
}
