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


function prepareFlowData(flows, apps) {
    const entitiesById = _.keyBy(apps, 'id');

    const enrichedFlows = _.map(flows, f => ({
        source: entitiesById[f.source.id] || { ...f.source, isNeighbour: true },
        target: entitiesById[f.target.id] || { ...f.target, isNeighbour: true },
        dataType: f.dataType
    }));

    const entities = _.chain(enrichedFlows)
        .map(f => ([f.source, f.target]))
        .flatten()
        .uniqBy(a => a.id)
        .value();

    return {
        flows: enrichedFlows,
        entities
    };
}


function loadDataFlows(dataFlowStore, groupApps) {
    const groupAppIds = _.map(groupApps, 'id');

    return dataFlowStore.findByAppIds(groupAppIds)
        .then(fs => prepareFlowData(fs, groupApps));

}


function service(appStore,
                 appCapabilityStore,
                 orgUnitUtils,
                 changeLogStore,
                 dataFlowStore,
                 involvementStore,
                 ratingStore,
                 perspectiveStore,
                 orgUnitStore,
                 ratedDataFlowDataService,
                 authSourceCalculator,
                 endUserAppStore,
                 assetCostStore,
                 complexityStore,
                 capabilityStore,
                 techStatsService,
                 $q) {

    const rawData = {};


    function loadAll(orgUnitId) {

        const promises = [
            orgUnitStore.findAll(),
            appStore.findByOrgUnitTree(orgUnitId),
            involvementStore.findPeopleByEntityReference('ORG_UNIT', orgUnitId),
            involvementStore.findByEntityReference('ORG_UNIT', orgUnitId),
            perspectiveStore.findByCode('BUSINESS'),
            changeLogStore.findByEntityReference('ORG_UNIT', orgUnitId)
        ];

        return $q.all(promises)
            .then(([
                orgUnits,
                apps,
                people,
                involvements,
                perspective,
                changeLogs]) => {

                const r = {
                    orgUnits,
                    apps,
                    involvements,
                    perspective,
                    changeLogs
                };

                Object.assign(rawData, r);
            })
            .then(() => loadAll2(orgUnitId))
    }

    function loadAll2(orgUnitId) {

        const appIds = _.map(rawData.apps, 'id');

        return $q.all([
            ratingStore.findByAppIds(appIds),
            loadDataFlows(dataFlowStore, rawData.apps),
            appCapabilityStore.findApplicationCapabilitiesByAppIds(appIds),
            capabilityStore.findByAppIds(appIds),
            ratedDataFlowDataService.findByOrgUnitTree(orgUnitId),  // use orgIds (ASC + DESC)
            authSourceCalculator.findByOrgUnit(orgUnitId),  // use orgIds(ASC)
            endUserAppStore.findByOrgUnitTree(orgUnitId),   // use orgIds(DESC)
            assetCostStore.findAppCostsByAppIds(appIds),
            complexityStore.findByAppIds(appIds),
            techStatsService.findByAppIds(appIds)
    ]).then(([
            capabilityRatings,
            dataFlows,
            rawAppCapabilities,
            capabilities,
            ratedDataFlows,
            authSources,
            endUserApps,
            assetCosts,
            complexity,
            techStats
        ]) => {

            const r = {
                orgUnitId,
                capabilityRatings,
                dataFlows,
                rawAppCapabilities,
                capabilities,
                ratedDataFlows,
                authSources,
                endUserApps,
                assetCosts,
                complexity,
                techStats
            };

            Object.assign(rawData, r);

            rawData.immediateHierarchy = orgUnitUtils.getImmediateHierarchy(rawData.orgUnits, orgUnitId);
            rawData.involvements = aggregatePeopleInvolvements(rawData.involvements, rawData.people);
            rawData.orgUnit = _.find(rawData.orgUnits, { id: orgUnitId });
            rawData.ratedFlows = new RatedFlowsData(rawData.ratedDataFlows, rawData.apps, rawData.orgUnits, orgUnitId);

            return rawData;
        });
    }


    return {
        loadAll
    };

}

service.$inject = [
    'ApplicationStore',
    'AppCapabilityStore',
    'OrgUnitUtilityService',
    'ChangeLogDataService',
    'DataFlowDataStore',
    'InvolvementDataService',
    'RatingStore',
    'PerspectiveStore',
    'OrgUnitStore',
    'RatedDataFlowDataService',
    'AuthSourcesCalculator',
    'EndUserAppStore',
    'AssetCostStore',
    'ComplexityStore',
    'CapabilityStore',
    'TechnologyStatisticsService',
    '$q'
];


export default service;
