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


function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/change-log`;

    const findByEntityReference = (ref, limit = 30) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${BASE}/${ref.kind}/${ref.id}`, {params: {limit}})
            .then(result => result.data);
    };

    const findByEntityReferenceForDate = (ref, date) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${BASE}/${ref.kind}/${ref.id}`, {params: {date}})
            .then(result => result.data);
    };

    const findByEntityReferenceForDateRange = (ref, startDate, endDate) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${BASE}/${ref.kind}/${ref.id}/date-range`, {params: {startDate, endDate}})
            .then(result => result.data);
    };

    const findUnattestedChangesByEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${BASE}/${ref.kind}/${ref.id}/unattested`)
            .then(result => result.data);
    };

    const findForUserName = (userName, limit = null) =>
        $http.get(`${BASE}/user/${userName}`, {params: {limit}})
            .then(r => r.data);

    const findSummaries = (kind, options, limit = null) => {
        checkIsIdSelector(options);
        return $http
            .post(`${BASE}/summaries/${kind}`, options, {params: {limit}})
            .then(r => r.data);
    };

    return {
        findByEntityReference,
        findByEntityReferenceForDate,
        findByEntityReferenceForDateRange,
        findUnattestedChangesByEntityReference,
        findForUserName,
        findSummaries,
    };
}

store.$inject = [
    "$http",
    "BaseApiUrl"
];


const serviceName = "ChangeLogStore";


export const ChangeLogStore_API = {
    findByEntityReference: {
        serviceName,
        serviceFnName: "findByEntityReference",
        description: "finds change log entries for a given entity reference and limit (default: 30)"
    },
    findByEntityReferenceForDate: {
        serviceName,
        serviceFnName: "findByEntityReferenceForDate",
        description: "finds change log entries for a given entity reference and date"
    },
    findByEntityReferenceForDateRange: {
        serviceName,
        serviceFnName: "findByEntityReferenceForDateRange",
        description: "finds change log entries for a given entity reference and date range"
    },
    findUnattestedChangesByEntityReference: {
        serviceName,
        serviceFnName: "findUnattestedChangesByEntityReference",
        description: "finds change log entries for a given entity reference since its last set of attestations"
    },
    findForUserName: {
        serviceName,
        serviceFnName: "findForUserName",
        description: "finds change log entries for a given user name and limit (default: no limit)"
    },
    findSummaries: {
        serviceName,
        serviceFnName: "findSummaries",
        description: "finds tallies by date for all changes of the given kind for entities related to the given selector [desiredKind, selector, limit]"
    }
};


export default {
    store,
    serviceName
};

