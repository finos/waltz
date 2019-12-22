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

import _ from "lodash";


export function findUnknownDataTypeId(dataTypes = []) {
    const unknownDataType = _.find(dataTypes, dt => dt.unknown);
    if (! unknownDataType) {
        throw "Unknown data type not found!"
    }
    return unknownDataType.id;
}

export function findDeprecatedDataTypeIds(dataTypes = []) {
    return _.filter(dataTypes, dt => dt.deprecated)
          .map(dt => dt.id);
}

export function findNonConcreteDataTypeIds(dataTypes = []) {
    return _.filter(dataTypes, dt => !dt.concrete)
            .map(dt => dt.id);
}

export function enrichDataTypes(dataTypes = [], selectedDataTypeIds = []) {
    const enrich = (datatype) => {
        datatype.disable = !selectedDataTypeIds.includes(datatype.id)
            && !datatype.concrete;
        return datatype;
    };
    return _.map(dataTypes, enrich);
}