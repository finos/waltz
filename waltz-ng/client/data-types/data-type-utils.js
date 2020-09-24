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
import {CORE_API} from "../common/services/core-api-utils";


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





function prepareUsageData(decoratorUsages = [], decorators = []) {
    const datatypeUsageCharacteristicsById = _.keyBy(decoratorUsages, d => d.dataTypeId);

    return _.map(
        decorators,
        d => {
            const usageInfo = _.get(
                datatypeUsageCharacteristicsById,
                [d.dataTypeId],
                {});
            return Object.assign(
                {},
                usageInfo,
                { readOnly: d.isReadOnly});
        });
}

/**
 * Given a parent will load all decorators and also determine usage characteristics.
 *
 * @param $q
 * @param serviceBroker
 * @param parentEntityRef
 * @param force
 * @returns array [ { dataTypeId, physicalFlowUsageCount, readOnly }, ... ]
 */
export function loadUsageData($q, serviceBroker, parentEntityRef, force) {
    const decoratorPromise = serviceBroker
        .loadViewData(
            CORE_API.DataTypeDecoratorStore.findByEntityReference,
            [parentEntityRef],
            { force })
        .then(r => r.data)
        .then(decorators => _.map(decorators, d => ({
            lastUpdatedAt: d.lastUpdatedAt,
            lastUpdatedBy: d.lastUpdatedBy,
            provenance: d.provenance,
            dataTypeId: d.decoratorEntity.id,
            dataFlowId: d.dataFlowId,
            isReadOnly: d.isReadonly // note case change!
        })));

    const datatypeUsageCharacteristicsPromise = serviceBroker
        .loadViewData(CORE_API.DataTypeDecoratorStore.findDatatypeUsageCharacteristics,
            [parentEntityRef],
            { force })
        .then(r => r.data);

    return $q
        .all([decoratorPromise, datatypeUsageCharacteristicsPromise])
        .then(([decorators, decoratorUsages]) => prepareUsageData(decoratorUsages, decorators));
}
