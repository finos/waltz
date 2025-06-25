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

export function store($http, baseApiUrl) {
    const base = `${baseApiUrl}/attestation-pre-check`;



    const logicalFlowCheck = (ref) => {
        checkIsEntityRef(ref);

        return $http
            .get(`${base}/logical-flow/entity/${ref.kind}/${ref.id}`)
            .then(r => r.data);
    };

    const viewpointCheck = (parentRef, categoryId) => {
        checkIsEntityRef(parentRef);

        return $http
            .get(`${base}/viewpoint/entity/${parentRef.kind}/${parentRef.id}/category/${categoryId}`)
            .then(r => r.data);
    };


    return {
        logicalFlowCheck,
        viewpointCheck
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


const serviceName = 'AttestationPreCheckStore';


export const AttestationPreCheckStore_API = {
    logicalFlowCheck: {
        serviceName,
        serviceFnName: 'logicalFlowCheck',
        description: 'logicalFlowCheck for a given entity [ref]'
    },
    viewpointCheck: {
        serviceName,
        serviceFnName: 'viewpointCheck',
        description: 'viewpointCheck for a given entity [ref]'
    }
};


export default {
    serviceName,
    store
};
