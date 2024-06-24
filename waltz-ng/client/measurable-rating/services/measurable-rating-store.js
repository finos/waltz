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

    const getById = (id) => {
        return $http
            .get(`${baseUrl}/id/${id}`)
            .then(d => d.data);
    };

    const getViewById = (id) => {
        return $http
            .get(`${baseUrl}/id/${id}/view`)
            .then(d => d.data);
    };

    const findForEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${baseUrl}/entity/${ref.kind}/${ref.id}`)
            .then(d => d.data);
    };

    const getViewForEntity = (ref) => {
        return $http
            .get(`${baseUrl}/entity/${ref.kind}/${ref.id}/view`)
            .then(d => d.data);
    };

    const getViewByCategoryAndAppSelector = (categoryId, selector) => {
        return $http
            .post(`${baseUrl}/category/${categoryId}/view`, selector)
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

    const hasMeasurableRatings = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${baseUrl}/has-measurable-ratings`, options)
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
        getById,
        getViewById,
        getViewForEntity,
        getViewByCategoryAndAppSelector,
        findByMeasurableSelector,
        findByAppSelector,
        findByCategory,
        findForEntityReference,
        countByMeasurableCategory,
        statsByAppSelector,
        hasMeasurableRatings,
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
    getById: {
        serviceName,
        serviceFnName: "getById",
        description: "finds measurable rating by id"
    },
    getViewById: {
        serviceName,
        serviceFnName: "getViewById",
        description: "finds measurable rating, with measurable, decom, and replacement details by id"
    },
    getViewForEntity: {
        serviceName,
        serviceFnName: "getViewForEntity",
        description: "finds all details related to the measurable ratings for an entity"
    },
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
    getViewByCategoryAndAppSelector: {
        serviceName,
        serviceFnName: "getViewByCategoryAndAppSelector",
        description: "finds measurable ratings and primary assessments for ratings using an app id selector limited to a category"
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
    hasMeasurableRatings: {
        serviceName,
        serviceFnName: "hasMeasurableRatings",
        description: "return boolean if there are measurable ratings for this selector [options]"
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
