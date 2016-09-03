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
import {aggregatePeopleInvolvements} from "../../involvement/involvement-utils";


function service($q,
                 appStore,
                 appCapabilityStore,
                 orgUnitUtils,
                 changeLogStore,
                 dataFlowViewService,
                 entityStatisticStore,
                 involvementStore,
                 ratingStore,
                 perspectiveStore,
                 orgUnitStore,
                 ratedDataFlowDataService,
                 authSourceCalculator,
                 endUserAppStore,
                 assetCostViewService,
                 complexityStore,
                 capabilityStore,
                 techStatsService,
                 bookmarkStore,
                 sourceDataRatingStore) {

    const rawData = {};



    function loadAll(orgUnitId) {

        const appIdSelector = {
            entityReference: {
                id: orgUnitId,
                kind: 'ORG_UNIT'
            },
            scope: 'CHILDREN'
        };

        const promises = [
            orgUnitStore.findAll(),
            appStore.findBySelector(appIdSelector),
            involvementStore.findPeopleByEntityReference('ORG_UNIT', orgUnitId),
            involvementStore.findByEntityReference('ORG_UNIT', orgUnitId),
            perspectiveStore.findByCode('BUSINESS'),
            dataFlowViewService.initialise(orgUnitId, "ORG_UNIT", "CHILDREN"),
            changeLogStore.findByEntityReference('ORG_UNIT', orgUnitId),
            assetCostViewService.initialise(appIdSelector, 2016)
        ];

        return $q.all(promises)
            .then(([
                orgUnits,
                apps,
                people,
                involvements,
                perspective,
                dataFlows,
                changeLogs,
                assetCostData]) => {

                const appsWithManagement = _.map(apps, a => _.assign(a, {management: 'IT'}));

                const r = {
                    orgUnits,
                    apps: appsWithManagement,
                    involvements,
                    perspective,
                    dataFlows,
                    changeLogs,
                    assetCostData
                };

                Object.assign(rawData, r);
            })
            .then(() => loadAll2(orgUnitId))
    }


    function loadAll2(orgUnitId) {

        const selector = {
            entityReference: {
                kind: 'ORG_UNIT',
                id: orgUnitId
            },
            scope: 'CHILDREN'
        };

        const bulkPromise = $q.all([
            ratingStore.findByAppIdSelector(selector),
            appCapabilityStore.findApplicationCapabilitiesByAppIdSelector(selector),
            capabilityStore.findAll(),
            ratedDataFlowDataService.findByOrgUnitTree(orgUnitId),  // use orgIds (ASC + DESC)
            authSourceCalculator.findByOrgUnit(orgUnitId),  // use orgIds(ASC)
            endUserAppStore.findBySelector(selector),   // use orgIds(DESC)
            complexityStore.findBySelector(orgUnitId, 'ORG_UNIT', 'CHILDREN'),
            techStatsService.findBySelector(orgUnitId, 'ORG_UNIT', 'CHILDREN'),
            bookmarkStore.findByParent({id: orgUnitId, kind: 'ORG_UNIT'}),
            sourceDataRatingStore.findAll(),
            entityStatisticStore.findAllActiveDefinitions(selector)
        ]);

        const prepareRawDataPromise = bulkPromise
            .then(([
                capabilityRatings,
                rawAppCapabilities,
                capabilities,
                ratedDataFlows,
                authSources,
                endUserApps,
                complexity,
                techStats,
                bookmarks,
                sourceDataRatings,
                entityStatisticDefinitions
            ]) => {

                const endUserAppsWithManagement = _.map(_.cloneDeep(endUserApps),
                    a => _.assign(a, {
                        management: 'End User',
                        platform: a.kind,
                        kind: 'EUC',
                        overallRating: 'Z'
                    }));


                const combinedApps = _.concat(rawData.apps, endUserAppsWithManagement);


                const r = {
                    orgUnitId,
                    capabilityRatings,
                    rawAppCapabilities,
                    capabilities,
                    ratedDataFlows,
                    authSources,
                    endUserApps: endUserAppsWithManagement,
                    complexity,
                    techStats,
                    bookmarks,
                    sourceDataRatings,
                    entityStatisticDefinitions,
                    combinedApps
                };

                Object.assign(rawData, r);

                rawData.immediateHierarchy = orgUnitUtils.getImmediateHierarchy(rawData.orgUnits, orgUnitId);
                rawData.involvements = aggregatePeopleInvolvements(rawData.involvements, rawData.people);
                rawData.orgUnit = _.find(rawData.orgUnits, { id: orgUnitId });
                rawData.ratedFlows = new RatedFlowsData(rawData.ratedDataFlows, rawData.apps, rawData.orgUnits, orgUnitId);

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
    'OrgUnitUtilityService',
    'ChangeLogDataService',
    'DataFlowViewService',
    'EntityStatisticStore',
    'InvolvementStore',
    'RatingStore',
    'PerspectiveStore',
    'OrgUnitStore',
    'RatedDataFlowDataService',
    'AuthSourcesCalculator',
    'EndUserAppStore',
    'AssetCostViewService',
    'ComplexityStore',
    'CapabilityStore',
    'TechnologyStatisticsService',
    'BookmarkStore',
    'SourceDataRatingStore'
];


export default service;
