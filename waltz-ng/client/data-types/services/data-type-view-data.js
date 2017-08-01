/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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


function service($q,
                 appStore,
                 dataTypeUsageStore,
                 dataTypeService,
                 sourceDataRatingStore,
                 authSourcesStore,
                 orgUnitStore
) {

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
            dataTypeService.loadDataTypes(),
            appStore.findBySelector(dataTypeIdSelector)
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
            sourceDataRatingStore.findAll(),
            dataTypeUsageStore.findForUsageKindByDataTypeIdSelector('ORIGINATOR', selector),
            dataTypeUsageStore.findForUsageKindByDataTypeIdSelector('DISTRIBUTOR', selector)
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

        authSourcesStore
            .findByDataTypeIdSelector(selector)
            .then(authSources => rawData.authSources = authSources)
            .then(authSources => _.chain(authSources).map('parentReference.id').uniq().value() )
            .then(orgUnitStore.findByIds)
            .then(orgUnits => rawData.orgUnits = orgUnits);

        authSourcesStore
            .calculateConsumersForDataTypeIdSelector(selector)
            .then(d => rawData.authSourceConsumers = d);

        return prepareRawDataPromise;
    }


    return {
        loadAll
    };

}

service.$inject = [
    '$q',
    'ApplicationStore',
    'DataTypeUsageStore',
    'DataTypeService',
    'SourceDataRatingStore',
    'AuthSourcesStore',
    'OrgUnitStore'
];


export default service;