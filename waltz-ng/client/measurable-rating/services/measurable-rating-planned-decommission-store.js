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

function store($http, baseApiUrl) {

    const baseUrl = `${baseApiUrl}/measurable-rating-planned-decommission`;

    const findForEntityRef = (ref) => $http
        .get(`${baseUrl}/entity/${ref.kind}/${ref.id}`)
        .then(d => d.data);

    const findForReplacingEntityRef = (ref) => $http
        .get(`${baseUrl}/replacing-entity/${ref.kind}/${ref.id}`)
        .then(d => d.data);

    const save = (measurableRatingId, dateChange) => $http
        .post(`${baseUrl}/measurable-rating/${measurableRatingId}`, dateChange)
        .then(d => d.data);

    const remove = (id) => $http
        .delete(`${baseUrl}/id/${id}`)
        .then(d => d.data);

    return {
        findForEntityRef,
        findForReplacingEntityRef,
        save,
        remove
    };
}

store.$inject = ["$http", "BaseApiUrl"];


const serviceName = "MeasurableRatingPlannedDecommissionStore";


export default {
    serviceName,
    store
};


export const MeasurableRatingPlannedDecommissionStore_API = {
    findForEntityRef: {
        serviceName,
        serviceFnName: "findForEntityRef",
        description: "finds all measurable decommission dates for a given entity"
    },
    findForReplacingEntityRef: {
        serviceName,
        serviceFnName: "findForReplacingEntityRef",
        description: "finds all measurable decommissions where the given entity is the replacement"
    },
    save: {
        serviceName,
        serviceFnName: "save",
        description: "saves decommission date,  [ref, measurableId, fieldChange{newVal, oldVal}]"
    },
    remove: {
        serviceName,
        serviceFnName: "remove",
        description: "removes decommission date and any replacement applications"
    }
};