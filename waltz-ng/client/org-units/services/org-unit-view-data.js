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
import {aggregatePeopleInvolvements} from "../../involvement/involvement-utils";
import {CORE_API} from "../../common/services/core-api-utils";

function mkRef(orgUnitId) {
    return {
        kind: 'ORG_UNIT',
        id: orgUnitId
    };
}


function mkSelector(orgUnitId) {
    return {
        entityReference: mkRef(orgUnitId),
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


function loadApps(serviceBroker, selector, holder) {
    return serviceBroker
        .loadViewData(CORE_API.ApplicationStore.findBySelector, [selector])
        .then(r => _.map(
            r.data,
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
        .determineAuthSourcesForOrgUnit(id)
        .then(r => holder.authSources = r);
}


function loadComplexity(store, id, holder) {
    return store
        .findBySelector(id, 'ORG_UNIT', 'CHILDREN')
        .then(r => holder.complexity = r);
}


function loadSourceDataRatings(serviceBroker, holder) {
    return serviceBroker
        .loadViewData(CORE_API.SourceDataRatingStore.findAll, [])
        .then(r => holder.sourceDataRatings = r.data);
}


function loadTechStats(serviceBroker, id, holder) {
    const selector = mkSelector(id);

    return serviceBroker
        .loadViewData(CORE_API.TechnologyStatisticsService.findBySelector, [selector])
        .then(r => holder.techStats = r.data);
}


function service($q,
                 serviceBroker,
                 assetCostViewService,
                 authSourcesStore,
                 complexityStore,
                 involvementStore,
                 logicalFlowViewService,
                 orgUnitStore) {

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
            .then(() => rawData.combinedApps = _.concat(rawData.apps, rawData.endUserApps))
            .then(() => loadThirdWave(orgUnitId))
            .then(() => rawData);
    }


    function loadFirstWave(orgUnitId) {
        const selector = mkSelector(orgUnitId);

        rawData.entityReference = selector.entityReference;
        rawData.orgUnitId = orgUnitId;

        return $q.all([
            loadOrgUnit(orgUnitStore, orgUnitId, rawData),
            loadImmediateHierarchy(orgUnitStore, orgUnitId, rawData),
            loadApps(serviceBroker, selector, rawData),
            initialiseAssetCosts(assetCostViewService, selector, rawData)
        ]);
    }


    function loadSecondWave(orgUnitId) {
        return $q.all([
            initialiseDataFlows(logicalFlowViewService, orgUnitId, rawData),
            loadInvolvement(involvementStore, orgUnitId, rawData),
            loadAuthSources(authSourcesStore, orgUnitId, rawData),
            loadComplexity(complexityStore, orgUnitId, rawData)
        ]);
    }


    function loadThirdWave(orgUnitId) {
        return $q.all([
            loadSourceDataRatings(serviceBroker, rawData),
            loadTechStats(serviceBroker, orgUnitId, rawData)
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


    function loadInvolvements(orgUnitId) {
        return loadInvolvement(involvementStore, orgUnitId, rawData);
    }

    return {
        data: rawData,
        loadAll,
        loadAllCosts,
        loadFlowDetail,
        loadOrgUnitDescendants,
        loadInvolvements
    };

}


service.$inject = [
    '$q',
    'ServiceBroker',
    'AssetCostViewService',
    'AuthSourcesStore',
    'ComplexityStore',
    'InvolvementStore',
    'LogicalFlowViewService',
    'OrgUnitStore'
];


export default service;
