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
import {entity} from "../../common/services/enums/entity";

function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/data-type-decorator`;

    const findByEntityReference = (ref) => {
        return $http
            .get(`${BASE}/entity/${ref.kind}/${ref.id}`)
            .then(result => result.data);
    };


    const findBySelector = (options, targetKind) => {
        return $http
            .post(`${BASE}/selector/targetKind/${targetKind}`, options)
            .then(result => result.data);
    };


    // ONLY FOR LOGICAL FLOWS
    const findByFlowIds = (flowIds = [], kind = entity.LOGICAL_DATA_FLOW.key) => {
        return $http
            .post(`${BASE}/flow-ids/kind/${kind}`, flowIds)
            .then(result => result.data);
    };


    const save = (ref, command) => {
        checkIsEntityRef(ref);
        return $http.post(`${BASE}/save/entity/${ref.kind}/${ref.id}`, command)
            .then(result => result.data);
    };

    // ONLY FOR LOGICAL FLOWS
    const findDatatypeUsageCharacteristics = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${BASE}/entity/${ref.kind}/${ref.id}/usage-characteristics`)
            .then(result => result.data);
    };

    return {
        findBySelector,
        findByEntityReference,
        findByFlowIds,
        save,
        findDatatypeUsageCharacteristics,
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


const serviceName = "DataTypeDecoratorStore";


export const DataTypeDecoratorStore_API = {
    findBySelector: {
        serviceName,
        serviceFnName: "findBySelector",
        description: "finds data types for a given selector"
    },
    findByEntityReference: {
        serviceName,
        serviceFnName: "findByEntityReference",
        description: "finds by entity reference for data types"
    },
    findByFlowIds: {
        serviceName,
        serviceFnName: "findByFlowIds",
        description: "finds data types for flow ids"
    },
    save: {
        serviceName,
        serviceFnName: "save",
        description: "saves (inserts/deletes) data types for a given entity ref"
    },
    findDatatypeUsageCharacteristics: {
        serviceName,
        serviceFnName: "findDatatypeUsageCharacteristics",
        description: "finds datatype usage characteristics for this entity"
    }
};

export default {
    store,
    serviceName
};