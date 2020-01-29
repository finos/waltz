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
import {checkIsEntityRef} from "../../common/checks";


function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/change-log`;

    const findByEntityReference = (ref, limit = 30) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${BASE}/${ref.kind}/${ref.id}`, {params: {limit}})
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

    return {
        findByEntityReference,
        findUnattestedChangesByEntityReference,
        findForUserName
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
    findUnattestedChangesByEntityReference: {
        serviceName,
        serviceFnName: "findUnattestedChangesByEntityReference",
        description: "finds change log entries for a given entity reference since its last set of attestations"
    },
    findForUserName: {
        serviceName,
        serviceFnName: "findForUserName",
        description: "'finds change log entries for a given user name and limit (default: no limit)"
    }
};


export default {
    store,
    serviceName
};

