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
import {CORE_API} from "../../common/services/core-api-utils";


function service($q,
                 serviceBroker) {

    const rawData = {};

    function loadAll(dataTypeId) {
        const entityReference = {
            id: dataTypeId,
            kind: 'DATA_TYPE'
        };

        const dataTypeIdSelector = {
            entityReference,
            scope: 'CHILDREN'
        };

        const promises = [
            serviceBroker
                .loadAppData(CORE_API.DataTypeStore.findAll)
                .then(r => r.data),
            serviceBroker
                .loadViewData(CORE_API.ApplicationStore.findBySelector, [dataTypeIdSelector])
                .then(r => r.data)
        ];

        return $q.all(promises)
            .then(([
                dataTypes,
                apps,
            ]) => {
                const appsWithManagement = _.map(apps, a => _.assign(a, {management: 'IT'}));
                const r = {
                    dataTypes,
                    apps: appsWithManagement
                };

                Object.assign(rawData, r);
            })
            .then(() => loadAll2(dataTypeId))
            .then(() => rawData);
    }

    function loadAll2(dataTypeId) {

        const selector = {
            entityReference: {
                kind: 'DATA_TYPE',
                id: dataTypeId
            },
            scope: 'CHILDREN'
        };

        const bulkPromise = $q.all([
            serviceBroker
                .loadAppData(CORE_API.SourceDataRatingStore.findAll)
                .then(r => r.data),
            serviceBroker
                .loadViewData(
                    CORE_API.DataTypeUsageStore.findForUsageKindByDataTypeIdSelector,
                    ['ORIGINATOR', selector])
                .then(r => r.data),
            serviceBroker
                .loadViewData(
                    CORE_API.DataTypeUsageStore.findForUsageKindByDataTypeIdSelector,
                    ['DISTRIBUTOR', selector])
                .then(r => r.data)
        ]);

        const prepareRawDataPromise = bulkPromise
            .then(([
                sourceDataRatings,
                flowOriginators,
                flowDistributors
            ]) => {

                const r = {
                    dataTypeId,
                    sourceDataRatings,
                    flowOriginators,
                    flowDistributors
                };

                Object.assign(rawData, r);

                rawData.dataType = _.find(rawData.dataTypes, { id: dataTypeId });

                return rawData;
            });



        return prepareRawDataPromise;
    }


    return {
        loadAll
    };

}

service.$inject = [
    '$q',
    'ServiceBroker'
];


export default service;