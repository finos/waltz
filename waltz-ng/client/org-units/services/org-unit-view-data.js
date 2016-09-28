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
import {aggregatePeopleInvolvements} from "../../involvement/involvement-utils";


function mkSelector(orgUnitId) {
    return {
        entityReference: {
            kind: 'ORG_UNIT',
            id: orgUnitId
        },
        scope: 'CHILDREN'
    };
}


function service($q,
                 appStore,
                 appCapabilityStore,
                 changeLogStore,
                 dataFlowViewService,
                 entityStatisticStore,
                 involvementStore,
                 ratingStore,
                 orgUnitStore,
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
        const appIdSelector = mkSelector(orgUnitId);

        const promises = [
            orgUnitStore.getById(orgUnitId),
            orgUnitStore.findImmediateHierarchy(orgUnitId),
            appStore.findBySelector(appIdSelector),
            involvementStore.findPeopleByEntityReference('ORG_UNIT', orgUnitId),
            involvementStore.findByEntityReference('ORG_UNIT', orgUnitId),
            dataFlowViewService.initialise(orgUnitId, "ORG_UNIT", "CHILDREN"),
            assetCostViewService.initialise(appIdSelector, 2016)
        ];

        return $q.all(promises)
            .then(([
                orgUnit,
                immediateHierarchy,
                apps,
                people,
                involvements,
                dataFlows,
                assetCostData]) => {

                const appsWithManagement = _.map(
                    apps,
                    a => _.assign(a, { management: 'IT' }));

                const r = {
                    orgUnit,
                    entityReference: appIdSelector.entityReference,
                    immediateHierarchy,
                    apps: appsWithManagement,
                    involvements,
                    dataFlows,
                    assetCostData
                };

                Object.assign(rawData, r);
            })
            .then(() => loadAll2(orgUnitId))
            .then(() => loadChangeLogs(orgUnitId, rawData))
            .then(() => rawData);
    }

    function loadChangeLogs(orgUnitId, holder = {}) {
        return changeLogStore
            .findByEntityReference('ORG_UNIT', orgUnitId)
            .then(changeLogs => holder.changeLogs = changeLogs);
    }

    function loadAll2(orgUnitId) {
        const selector = mkSelector(orgUnitId);

        const bulkPromise = $q.all([
            ratingStore.findByAppIdSelector(selector),
            appCapabilityStore.findApplicationCapabilitiesByAppIdSelector(selector),
            capabilityStore.findAll(),
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

                rawData.involvements = aggregatePeopleInvolvements(rawData.involvements, rawData.people);

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
        return dataFlowViewService
            .loadDetail()
            .then(dataFlows => rawData.dataFlows = dataFlows);
    }


    function loadOrgUnitDescendants(orgUnitId) {
        return orgUnitStore
            .findDescendants(orgUnitId)
            .then(descendants => rawData.orgUnitDescendants = descendants);
    }


    return {
        loadAll,
        selectAssetBucket,
        loadFlowDetail,
        loadOrgUnitDescendants
    };

}

service.$inject = [
    '$q',
    'ApplicationStore',
    'AppCapabilityStore',
    'ChangeLogDataService',
    'DataFlowViewService',
    'EntityStatisticStore',
    'InvolvementStore',
    'RatingStore',
    'OrgUnitStore',
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
