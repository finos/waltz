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
import {checkIsIdSelector} from "../../common/checks";


function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/change-log-summaries`;


    const findSummariesForKindBySelectorForDateRange = (kind, selector, startDate, endDate, limit) => {
        checkIsIdSelector(selector);
        return $http
            .post(`${BASE}/kind/${kind}/selector`, selector, {params: {limit, startDate, endDate}})
            .then(result => result.data);
    };

    return {
        findSummariesForKindBySelectorForDateRange
    };
}

store.$inject = [
    "$http",
    "BaseApiUrl"
];


const serviceName = "ChangeLogSummariesStore";


export const ChangeLogSummariesStore_API = {
    findSummariesForKindBySelectorForDateRange: {
        serviceName,
        serviceFnName: "findSummariesForKindBySelectorForDateRange",
        description: "finds changeLogTally for a given child kind and selector, start date and end date"
    },
};


export default {
    store,
    serviceName
};

