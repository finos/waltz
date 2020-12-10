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

import {enums} from "../../common/services/enums";
import {nest} from "d3-collection";

let enumsPromise = null;

function service(enumValueStore) {

    const loadEnums = (force = false) => {
        if (force || (enumsPromise == null)) {
            enumsPromise = enumValueStore
                .findAll()
                .then(d => {
                    const serverEnums = nest()
                        .key(e => e.type)
                        .key(e => e.key)
                        .rollup(vs => vs[0])
                        .object(d);

                    return Object.assign({}, enums, serverEnums);
                });
        }
        return enumsPromise;
    };


    loadEnums();

    return {
        loadEnums,
    };
}


service.$inject = [
    "EnumValueStore"
];


export default service;
