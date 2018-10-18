/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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