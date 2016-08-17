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
import RatedFlowsData from "../../data-flow/RatedFlowsData";


function service($q,
                 appStore,
                 appCapabilityStore,
                 changeLogStore,
                 dataFlowViewService,
                 dataFlowStore,
                 entityStatisticStore,
                 ratingStore,
                 dataTypeService,
                 assetCostViewService,
                 complexityStore,
                 capabilityStore,
                 techStatsService,
                 bookmarkStore,
                 sourceDataRatingStore) {

    const rawData = {};



    function loadAll(dataTypeId) {

        const appIdSelector = {
            entityReference: {
                id: dataTypeId,
                kind: 'DATA_TYPE'
            },
            scope: 'CHILDREN'
        };

        const promises = [
            dataTypeService.loadDataTypes(),
            appStore.findBySelector(appIdSelector),
            dataFlowViewService.initialise(dataTypeId, "DATA_TYPE", "CHILDREN"),
            dataFlowStore.countByDataType(),
            changeLogStore.findByEntityReference('DATA_TYPE', dataTypeId),
            assetCostViewService.initialise(appIdSelector, 2016)
        ];

        return $q.all(promises)
            .then(([
                dataTypes,
                apps,
                dataFlows,
                dataFlowTallies,
                changeLogs,
                assetCostData]) => {

                const appsWithManagement = _.map(apps, a => _.assign(a, {management: 'IT'}));

                const r = {
                    dataTypes,
                    apps: appsWithManagement,
                    dataFlows,
                    dataFlowTallies,
                    changeLogs,
                    assetCostData
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
                rawData.ratedFlows = new RatedFlowsData(rawData.ratedDataFlows, rawData.apps, rawData.orgUnits, dataTypeId);

                return rawData;
            });

        return prepareRawDataPromise;
    }


    function selectAssetBucket(bucket) {
        assetCostViewService.selectBucket(bucket);
        assetCostViewService.loadDetail()
            .then(data => rawData.assetCostData = data);
    }


    function loadFlowDetail() {
        dataFlowViewService.loadDetail();
    }


    return {
        loadAll,
        selectAssetBucket,
        loadFlowDetail
    };

}

service.$inject = [
    '$q',
    'ApplicationStore',
    'AppCapabilityStore',
    'ChangeLogDataService',
    'DataFlowViewService',
    'DataFlowDataStore',
    'EntityStatisticStore',
    'RatingStore',
    'DataTypeService',
    'AssetCostViewService',
    'ComplexityStore',
    'CapabilityStore',
    'TechnologyStatisticsService',
    'BookmarkStore',
    'SourceDataRatingStore'
];


export default service;
