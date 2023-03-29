

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

export function store($http, BaseApiUrl) {
    const base = BaseApiUrl + "/rating-scheme";


    function findAll() {
        return $http
            .get(base)
            .then(result => result.data);
    }

    function getById(id) {
        return $http
            .get(`${base}/id/${id}`)
            .then(result => result.data);
    }

    const findRatingsForEntityAndMeasurableCategory = (ref, categoryId) => $http
        .get(`${base}/items/kind/${ref.kind}/id/${ref.id}/category-id/${categoryId}`)
        .then(d => d.data);

    const findRatingsSchemeItems = (assessmentDefinitionId) => $http
        .get(`${base}/items/assessment-definition-id/${assessmentDefinitionId}`)
        .then(d => d.data);

    const findAllRatingsSchemeItems = () => $http
        .get(`${base}/items`)
        .then(d => d.data);


    return {
        findAll,
        getById,
        findRatingsForEntityAndMeasurableCategory,
        findRatingsSchemeItems,
        findAllRatingsSchemeItems
    };
}

store.$inject = ["$http", "BaseApiUrl"];


export const serviceName = "RatingSchemeStore";

export const RatingSchemeStore_API = {
    findAll: {
        serviceName,
        serviceFnName: "findAll",
        description: "executes findAll"
    },
    getById: {
        serviceName,
        serviceFnName: "getById",
        description: "executes getById"
    },
    findRatingsForEntityAndMeasurableCategory: {
        serviceName,
        serviceFnName: "findRatingsForEntityAndMeasurableCategory",
        description: "returns list of rating scheme items for entity and measurable category id (ref, categoryId)"
    },
    findRatingsSchemeItems: {
        serviceName,
        serviceFnName: "findRatingsSchemeItems",
        description: "returns all rating scheme items for an assessment definition"
    },
    findAllRatingsSchemeItems: {
        serviceName,
        serviceFnName: "findAllRatingsSchemeItems",
        description: "returns all rating scheme items"
    }
};

