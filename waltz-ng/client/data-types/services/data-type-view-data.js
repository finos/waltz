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