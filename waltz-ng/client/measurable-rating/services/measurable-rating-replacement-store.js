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

    const baseUrl = `${baseApiUrl}/measurable-rating-replacement`;

    const findForEntityRef = (ref) => $http
        .get(`${baseUrl}/entity/${ref.kind}/${ref.id}`)
        .then(d => d.data);

    const save = (decommId, replacement) => $http
        .post(
            `${baseUrl}/decomm-id/${decommId}/entity/${replacement.replacementApp.kind}/${replacement.replacementApp.id}`,
            replacement.commissionDate)
        .then(d => d.data);

    const remove = (decommId, replacementId) => $http
        .delete(`${baseUrl}/decomm-id/${decommId}/replacement-id/${replacementId}`)
        .then(d => d.data);

    return {
        findForEntityRef,
        remove,
        save
    };
}

store.$inject = ["$http", "BaseApiUrl"];


const serviceName = "MeasurableRatingReplacementStore";

export default {
    serviceName,
    store
};

export const MeasurableRatingReplacementStore_API = {
    findForEntityRef: {
        serviceName,
        serviceFnName: "findForEntityRef",
        description: "finds all replacement apps for a given entity"
    },
    remove: {
        serviceName,
        serviceFnName: "remove",
        description: "removes a replacement app for a given decommission  [replacementId]"
    },
    save: {
        serviceName,
        serviceFnName: "save",
        description: "saves a replacement app for a given decommission"
    }
};