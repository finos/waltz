/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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


function loadMeasurables(store, selector, holder) {
    return store
        .findMeasurablesBySelector(selector)
        .then(measurables => holder.measurables = measurables);
}


function loadMeasurableRatings(store, selector, holder) {
    store
        .statsByAppSelector(selector)
        .then(ratings => holder.measurableRatings = ratings);
}


function service($q,
                 appStore,
                 assetCostViewService,
                 authSourceCalculator,
                 bookmarkStore,
                 changeLogStore,
                 complexityStore,
                 endUserAppStore,
                 entityStatisticStore,
                 involvementStore,
                 logicalFlowViewService,
                 measurableStore,
                 measurableRatingStore,
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
            loadMeasurables(measurableStore, selector, rawData),
            loadMeasurableRatings(measurableRatingStore, selector, rawData),
            initialiseAssetCosts(assetCostViewService, selector, rawData)
        ]);
    }


    function loadSecondWave(orgUnitId) {
        const selector = mkSelector(orgUnitId);

        return $q.all([
            initialiseDataFlows(logicalFlowViewService, orgUnitId, rawData),
            loadInvolvement(involvementStore, orgUnitId, rawData),
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


    function loadAllCosts() {
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


    function loadRatingsDetail(orgUnitId) {
        return rawData.measurableRatingsDetail
            ? $q.resolve(rawData.measurableRatingsDetail)
            : measurableRatingStore
                .findByAppSelector(mkSelector(orgUnitId))
                .then(rs => rawData.measurableRatingsDetail = rs);
    };

    return {
        data: rawData,
        loadAll,
        loadAllCosts,
        loadFlowDetail,
        loadOrgUnitDescendants,
        loadRatingsDetail
    };

}


service.$inject = [
    '$q',
    'ApplicationStore',
    'AssetCostViewService',
    'AuthSourcesCalculator',
    'BookmarkStore',
    'ChangeLogStore',
    'ComplexityStore',
    'EndUserAppStore',
    'EntityStatisticStore',
    'InvolvementStore',
    'LogicalFlowViewService',
    'MeasurableStore',
    'MeasurableRatingStore',
    'OrgUnitStore',
    'PhysicalFlowLineageStore',
    'SourceDataRatingStore',
    'TechnologyStatisticsService',
];


export default service;
