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
                 techStatsService) {

    const rawData = {};

    function loadAll(orgUnitId) {

        const promises = [
            orgUnitStore.findAll(),
            appStore.findByOrgUnitTree(orgUnitId),
            involvementStore.findPeopleByEntityReference('ORG_UNIT', orgUnitId),
            involvementStore.findByEntityReference('ORG_UNIT', orgUnitId),
            perspectiveStore.findByCode('BUSINESS'),
            dataFlowViewService.initialise(orgUnitId, "ORG_UNIT", "CHILDREN"),
            changeLogStore.findByEntityReference('ORG_UNIT', orgUnitId),
            assetCostViewService.initialise(orgUnitId, 'ORG_UNIT', 'CHILDREN', 2015),
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

                const r = {
                    orgUnits,
                    apps,
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

        const appIds = _.map(rawData.apps, 'id');

        return $q.all([
            ratingStore.findByAppIds(appIds),
            appCapabilityStore.findApplicationCapabilitiesByAppIds(appIds),
            capabilityStore.findByAppIds(appIds),
            ratedDataFlowDataService.findByOrgUnitTree(orgUnitId),  // use orgIds (ASC + DESC)
            authSourceCalculator.findByOrgUnit(orgUnitId),  // use orgIds(ASC)
            endUserAppStore.findByOrgUnitTree(orgUnitId),   // use orgIds(DESC)
            complexityStore.findByAppIds(appIds),
            techStatsService.findByAppIds(appIds, orgUnitId, 'ORG_UNIT', 'CHILDREN')
    ]).then(([
            capabilityRatings,
            rawAppCapabilities,
            capabilities,
            ratedDataFlows,
            authSources,
            endUserApps,
            complexity,
            techStats
        ]) => {

            const r = {
                orgUnitId,
                capabilityRatings,
                rawAppCapabilities,
                capabilities,
                ratedDataFlows,
                authSources,
                endUserApps,
                complexity,
                techStats,
            };

            Object.assign(rawData, r);

            rawData.immediateHierarchy = orgUnitUtils.getImmediateHierarchy(rawData.orgUnits, orgUnitId);
            rawData.involvements = aggregatePeopleInvolvements(rawData.involvements, rawData.people);
            rawData.orgUnit = _.find(rawData.orgUnits, { id: orgUnitId });
            rawData.ratedFlows = new RatedFlowsData(rawData.ratedDataFlows, rawData.apps, rawData.orgUnits, orgUnitId);

            return rawData;
        });
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
    'InvolvementDataService',
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
];


export default service;
