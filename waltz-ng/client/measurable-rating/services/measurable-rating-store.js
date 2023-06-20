/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */
import {checkIsEntityRef, checkIsIdSelector} from "../../common/checks";


function store($http, baseApiUrl) {

    const baseUrl = `${baseApiUrl}/measurable-rating`;
    const viewBaseUrl = `${baseApiUrl}/measurable-rating-view`;

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

    const statsByAppSelector = (params) => {
        checkIsIdSelector(params.options);
        return $http
            .post(`${baseUrl}/stats-by/app-selector`, params)
            .then(d => d.data);
    };

    const statsForRelatedMeasurables = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${baseUrl}/related-stats/measurable`, options)
            .then(d => d.data);
    };

    const countByMeasurableCategory = (id) => {
        return $http
            .get(`${baseUrl}/count-by/measurable/category/${id}`)
            .then(d => d.data);
    };

    const saveRatingItem = (ref, measurableId, rating = "Z") => {
        checkIsEntityRef(ref);
        return $http
            .post(`${baseUrl}/entity/${ref.kind}/${ref.id}/measurable/${measurableId}/rating`, rating)
            .then(d => d.data);
    };

    const saveRatingIsPrimary = (ref, measurableId, isPrimary = false) => {
        checkIsEntityRef(ref);
        return $http
            .post(`${baseUrl}/entity/${ref.kind}/${ref.id}/measurable/${measurableId}/is-primary`, isPrimary)
            .then(d => d.data);
    };

    const saveRatingDescription = (ref, measurableId, description = "") => {
        checkIsEntityRef(ref);
        return $http
            .post(`${baseUrl}/entity/${ref.kind}/${ref.id}/measurable/${measurableId}/description`, description)
            .then(d => d.data);
    };

    const remove = (ref, measurableId) => {
        checkIsEntityRef(ref);
        return $http
            .delete(`${baseUrl}/entity/${ref.kind}/${ref.id}/measurable/${measurableId}`)
            .then(d => d.data);
    };

    const removeByCategory = (ref, categoryId) => {
        checkIsEntityRef(ref);
        return $http
            .delete(`${baseUrl}/entity/${ref.kind}/${ref.id}/category/${categoryId}`)
            .then(d => d.data);
    };

    return {
        findByMeasurableSelector,
        findByAppSelector,
        findByCategory,
        findForEntityReference,
        countByMeasurableCategory,
        statsByAppSelector,
        statsForRelatedMeasurables,
        saveRatingItem,
        saveRatingIsPrimary,
        saveRatingDescription,
        remove,
        removeByCategory
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
    saveRatingItem: {
        serviceName,
        serviceFnName: "saveRatingItem",
        description: "saves a measurable rating item [ref, measurableId, rating]"
    },
    saveRatingIsPrimary: {
        serviceName,
        serviceFnName: "saveRatingIsPrimary",
        description: "saves a measurable rating primary indicator [ref, measurableId, primaryFlag]"
    },
    saveRatingDescription: {
        serviceName,
        serviceFnName: "saveRatingDescription",
        description: "saves a measurable rating description [ref, measurableId, description]"
    },
    remove: {
        serviceName,
        serviceFnName: "remove",
        description: "remove a measurable rating"
    },
    removeByCategory: {
        serviceName,
        serviceFnName: "removeByCategory",
        description: "remove all measurable ratings for an entity in a given category [entityRef, categoryId]"
    }
};


export default {
    serviceName,
    store
}
