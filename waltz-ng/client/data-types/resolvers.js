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
import _ from 'lodash';
import {CORE_API} from '../common/services/core-api-utils';


export function loadDataTypes(serviceBroker) {
    return serviceBroker
        .loadAppData(CORE_API.DataTypeStore.findAll, [])
        .then(r => r.data);
}

loadDataTypes.$inject = [
    'ServiceBroker'
];


export function dataTypeByIdResolver($stateParams, serviceBroker) {
    if(!$stateParams.id)
        throw "'id' not found in stateParams";

    return serviceBroker
        .loadAppData(CORE_API.DataTypeStore.findAll, [])
        .then(r => {
            const dataType = _.find(r.data, { id: $stateParams.id});
            if (dataType) {
                return dataType;
            } else {
                console.error(`data type with id: ${$stateParams.id} not found`);
                return null;
            }
        });
}

dataTypeByIdResolver.$inject = [
    '$stateParams',
    'ServiceBroker'
];


export function dataTypeByCodeResolver($stateParams, serviceBroker) {
    if(!$stateParams.code)
        throw "'code' not found in stateParams";

    return serviceBroker
        .loadAppData(CORE_API.DataTypeStore.findAll, [])
        .then(r => {
            const dataType = _.find(r.data, { code: $stateParams.code});
            if (dataType) {
                return dataType;
            } else {
                console.error(`data type with code: ${$stateParams.code} not found`);
                return null;
            }
        });
}

dataTypeByCodeResolver.$inject = [
    '$stateParams',
    'ServiceBroker'
];