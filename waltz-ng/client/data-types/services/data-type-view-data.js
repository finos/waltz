/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
 */

import _ from "lodash";


function service($q,
                 appStore,
                 appCapabilityStore,
                 changeLogStore,
                 dataFlowStore,
                 entityStatisticStore,
                 ratingStore,
                 dataTypeService,
                 assetCostViewService,
                 complexityStore,
                 capabilityStore,
                 techStatsService,
                 bookmarkStore,
                 sourceDataRatingStore,
                 dataTypeUsageStore
                 // sourceSinkStore
) {

    const rawData = {};



    function loadAll(dataTypeId) {

        const dataTypeIdSelector = {
            entityReference: {
                id: dataTypeId,
                kind: 'DATA_TYPE'
            },
            scope: 'CHILDREN'
        };

        const promises = [
            dataTypeService.loadDataTypes(),
            appStore.findBySelector(dataTypeIdSelector),
            dataFlowStore.findByDataTypeIdSelector(dataTypeIdSelector),
            // dataFlowStore.calculateStatsForDataType(dataTypeIdSelector),
            changeLogStore.findByEntityReference('DATA_TYPE', dataTypeId),
            assetCostViewService.initialise(dataTypeIdSelector, 2016),
            dataTypeUsageStore.findUsageStatsForDataTypeSelector(dataTypeIdSelector)
        // sourceSinkStore.findByDataTypeSelector(dataTypeIdSelector)
        ];

        return $q.all(promises)
            .then(([
                dataTypes,
                apps,
                dataFlows,
                // dataFlowTallies,
                changeLogs,
                assetCostData,
                usageStats
                // sourceSinks
            ]) => {

                const appsWithManagement = _.map(apps, a => _.assign(a, {management: 'IT'}));

                const r = {
                    dataTypes,
                    apps: appsWithManagement,
                    dataFlows,
                    // dataFlowTallies,
                    changeLogs,
                    assetCostData,
                    usageStats
                    // sourceSinks
                };

                Object.assign(rawData, r);
            })
            .then(() => loadAll2(dataTypeId))
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
            ratingStore.findByAppIdSelector(selector),
            appCapabilityStore.findApplicationCapabilitiesByAppIdSelector(selector),
            capabilityStore.findAll(),
            complexityStore.findBySelector(dataTypeId, 'DATA_TYPE', 'CHILDREN'),
            techStatsService.findBySelector(dataTypeId, 'DATA_TYPE', 'CHILDREN'),
            bookmarkStore.findByParent({id: dataTypeId, kind: 'DATA_TYPE'}),
            sourceDataRatingStore.findAll(),
            entityStatisticStore.findAllActiveDefinitions(selector)
        ]);

        const prepareRawDataPromise = bulkPromise
            .then(([
                capabilityRatings,
                rawAppCapabilities,
                capabilities,
                complexity,
                techStats,
                bookmarks,
                sourceDataRatings,
                entityStatisticDefinitions
            ]) => {

                const r = {
                    dataTypeId,
                    capabilityRatings,
                    rawAppCapabilities,
                    capabilities,
                    complexity,
                    techStats,
                    bookmarks,
                    sourceDataRatings,
                    entityStatisticDefinitions,
                };

                Object.assign(rawData, r);

                rawData.dataType = _.find(rawData.dataTypes, { id: dataTypeId });
                // rawData.ratedFlows = new RatedFlowsData(rawData.ratedDataFlows, rawData.apps, rawData.orgUnits, dataTypeId);

                return rawData;
            });

        return prepareRawDataPromise;
    }


    function selectAssetBucket(bucket) {
        assetCostViewService.selectBucket(bucket);
        assetCostViewService.loadDetail()
            .then(data => rawData.assetCostData = data);
    }


    return {
        loadAll,
        selectAssetBucket,
    };

}

service.$inject = [
    '$q',
    'ApplicationStore',
    'AppCapabilityStore',
    'ChangeLogDataService',
    'DataFlowDataStore',
    'EntityStatisticStore',
    'RatingStore',
    'DataTypeService',
    'AssetCostViewService',
    'ComplexityStore',
    'CapabilityStore',
    'TechnologyStatisticsService',
    'BookmarkStore',
    'SourceDataRatingStore',
    'DataTypeUsageStore'
    // 'SourceSinkStore'
];


export default service;