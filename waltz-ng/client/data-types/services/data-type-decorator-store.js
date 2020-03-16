
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

export function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/data-type-decorator`;

    const findBySelector = (options) => {
        return $http.post(`${BASE}/find/selector`, options)
            .then(result => result.data);
    };

    const save = (ref, command) => {
        checkIsEntityRef(ref);
        return $http.post(`${BASE}/save/entity/${ref.kind}/${ref.id}/`, command)
            .then(result => result.data);
    };

    return {
        findBySelector: findBySelector,
        save
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


export const serviceName = 'DataTypeDecoratorStore';


export const DataTypeDecoratorStore_API = {
    findBySelector: {
        serviceName,
        serviceFnName: 'findBySelector',
        description: 'finds data types for a given selector'
    },
    save: {
        serviceName,
        serviceFnName: 'save',
        description: 'saves (inserts/deletes) data types for a given entity ref'
    }
};

