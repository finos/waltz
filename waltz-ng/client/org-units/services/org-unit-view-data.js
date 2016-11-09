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


function reset(data = {}) {
    _.each(data, (v, k) => data[k] = null);
}


function loadOrgUnit(orgUnitStore, id, holder) {
    return orgUnitStore
        .getById(id)
        .then(unit => holder.orgUnit = unit);
}


function loadImmediateHierarchy(store, id, holder) {
    return store
        .findImmediateHierarchy(id)
        .then(d => holder.immediateHierarchy = d);
}


function loadApps(store, selector, holder) {
    return store
        .findBySelector(selector)
        .then(apps => _.map(
            apps,
            a => _.assign(a, { management: 'IT' })))
        .then(apps => holder.apps = apps);
}


function loadInvolvement(store, id, holder) {
    return store
        .findPeopleByEntityReference('ORG_UNIT', id)
        .then(people => holder.people = people)
        .then(() => store.findByEntityReference('ORG_UNIT', id))
        .then(involvements => holder.involvements = aggregatePeopleInvolvements(involvements, holder.people));
}


function initialiseDataFlows(service, id, holder) {
    return service
        .initialise(id, "ORG_UNIT", "CHILDREN")
        .then(dataFlows => holder.dataFlows = dataFlows);
}


function initialiseAssetCosts(service, selector, holder) {
    return service
        .initialise(selector, 2016)
        .then(r => holder.assetCostData = r);
}


function loadAppCapabilities(store, selector, holder) {
    return store
        .findApplicationCapabilitiesByAppIdSelector(selector)
        .then(r => holder.appCapabilities = r);
}


function loadAllCapabilities(store, holder) {
    return store
        .findAll()
        .then(r => holder.capabilities = r);
}


function loadAuthSources(store, id, holder) {
    return store
        .findByOrgUnit(id)  // use orgIds(ASC)
        .then(r => holder.authSources = r);
}


function loadEndUserApps(store, selector, holder) {
    return store
        .findBySelector(selector)   // use orgIds(DESC)
        .then(apps => _.map(
                apps,
                a => _.assign(
                    a,
                    {
                        management: 'End User',
                        platform: a.kind,
                        kind: 'EUC',
                        overallRating: 'Z'
                    })))
        .then(apps => holder.endUserApps = apps);
}


function loadComplexity(store, id, holder) {
    return store
        .findBySelector(id, 'ORG_UNIT', 'CHILDREN')
        .then(r => holder.complexity = r);
}


function loadTechStats(service, id, holder) {
    return service
        .findBySelector(id, 'ORG_UNIT', 'CHILDREN')
        .then(r => holder.techStats = r);
}


function loadBookmarks(store, id, holder) {
    return store
        .findByParent({id, kind: 'ORG_UNIT'})
        .then(r => holder.bookmarks = r);
}


function loadSourceDataRatings(store, holder) {
    return store
        .findAll()
        .then(r => holder.sourceDataRatings = r);
}


function loadEntityStatisticDefinitions(store, selector, holder) {
    return store
        .findAllActiveDefinitions(selector)
        .then(r => holder.entityStatisticDefinitions = r);
}


function loadChangeLogs(store, id, holder = {}) {
    return store
        .findByEntityReference('ORG_UNIT', id)
        .then(changeLogs => holder.changeLogs = changeLogs);
}


function loadLineageReports(store, selector, holder) {
    return store
        .findLineageReportsBySelector(selector)
        .then(lineageReports => holder.lineageReports = lineageReports);
}


function service($q,
                 appStore,
                 appCapabilityStore,
                 assetCostViewService,
                 authSourceCalculator,
                 bookmarkStore,
                 capabilityStore,
                 changeLogStore,
                 complexityStore,
                 endUserAppStore,
                 entityStatisticStore,
                 involvementStore,
                 logicalFlowViewService,
                 orgUnitStore,
                 physicalFlowLineageStore,
                 sourceDataRatingStore,
                 techStatsService) {

    const rawData = {};

    /*
     * As this page is quite large we load it in 4 'waves' so that
     * we have some control over the order data is requested (and
     * hopefully displayed
     */
    function loadAll(orgUnitId) {
        reset(rawData);
        return loadFirstWave(orgUnitId)
            .then(() => loadSecondWave(orgUnitId))
            .then(() => loadThirdWave(orgUnitId))
            .then(() => rawData.combinedApps = _.concat(rawData.apps, rawData.endUserApps))
            .then(() => loadFourthWave(orgUnitId))
            .then(() => rawData);
    }


    function loadFirstWave(orgUnitId) {
        const selector = mkSelector(orgUnitId);

        rawData.entityReference = selector.entityReference;
        rawData.orgUnitId = orgUnitId;

        return $q.all([
            loadOrgUnit(orgUnitStore, orgUnitId, rawData),
            loadImmediateHierarchy(orgUnitStore, orgUnitId, rawData),
            loadApps(appStore, selector, rawData),
            initialiseAssetCosts(assetCostViewService, selector, rawData)
        ]);
    }


    function loadSecondWave(orgUnitId) {
        const selector = mkSelector(orgUnitId);

        return $q.all([
            initialiseDataFlows(logicalFlowViewService, orgUnitId, rawData),
            loadInvolvement(involvementStore, orgUnitId, rawData),
            loadAppCapabilities(appCapabilityStore, selector, rawData),
            loadAllCapabilities(capabilityStore, rawData),
            loadAuthSources(authSourceCalculator, orgUnitId, rawData),
            loadComplexity(complexityStore, orgUnitId, rawData),
            loadEntityStatisticDefinitions(entityStatisticStore, selector, rawData),
            loadLineageReports(physicalFlowLineageStore, selector, rawData)
        ]);
    }


    function loadThirdWave(orgUnitId) {
        const selector = mkSelector(orgUnitId);

        return $q.all([
            loadEndUserApps(endUserAppStore, selector, rawData),
            loadBookmarks(bookmarkStore, orgUnitId, rawData),
            loadTechStats(techStatsService, orgUnitId, rawData),
        ]);
    }


    function loadFourthWave(orgUnitId) {
        return $q.all([
            loadSourceDataRatings(sourceDataRatingStore, rawData),
            loadChangeLogs(changeLogStore, orgUnitId, rawData)
        ]);
    }


    function selectAssetBucket(bucket) {
        assetCostViewService.selectBucket(bucket);
        assetCostViewService.loadDetail()
            .then(data => rawData.assetCostData = data);
    }


    function loadFlowDetail() {
        return logicalFlowViewService
            .loadDetail()
            .then(dataFlows => rawData.dataFlows = dataFlows);
    }


    function loadOrgUnitDescendants(orgUnitId) {
        return orgUnitStore
            .findDescendants(orgUnitId)
            .then(descendants => rawData.orgUnitDescendants = descendants);
    }


    return {
        data: rawData,
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
    'AssetCostViewService',
    'AuthSourcesCalculator',
    'BookmarkStore',
    'CapabilityStore',
    'ChangeLogStore',
    'ComplexityStore',
    'EndUserAppStore',
    'EntityStatisticStore',
    'InvolvementStore',
    'LogicalFlowViewService',
    'OrgUnitStore',
    'PhysicalFlowLineageStore',
    'SourceDataRatingStore',
    'TechnologyStatisticsService',
];


export default service;
