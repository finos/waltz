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


const initModel = {
    managers: [],
    directs: [],
    person: null,
    involvements: {
        direct: [],
        indirect: [],
        allApps: []
    },
    apps: [],
    appIds: [],
    complexity: [],
    assetCostData: {},
    serverStats: null,
    dataFlows: []
};


function service(personStore,
                 involvementStore,
                 assetCostViewService,
                 complexityStore,
                 dataFlowViewService,
                 techStatsService,
                 $q) {

    const state = { model: initModel };

    function loadPeople(employeeId) {
        const personPromise = personStore.getByEmployeeId(employeeId)
            .then(person => state.model.person = person);

        const directsPromise = personStore.findDirects(employeeId)
            .then(directs => state.model.directs = directs);

        const managersPromise = personStore.findManagers(employeeId)
            .then(managers => state.model.managers = managers);

        return $q.all([personPromise, directsPromise, managersPromise]);
    }

    function loadApplications(employeeId) {
        return $q.all([
            involvementStore.findByEmployeeId(employeeId),
            involvementStore.findAppsForEmployeeId(employeeId)
        ]).then(([involvements, apps]) => {
            const appsById = _.keyBy(apps, 'id');

            const appInvolvements = _.filter(involvements, i => i.entityReference.kind === 'APPLICATION');
            const directlyInvolvedAppIds = _.map(appInvolvements, 'entityReference.id');

            const allAppIds = _.map(apps, 'id');
            const indirectlyInvolvedAppIds = _.difference(allAppIds, directlyInvolvedAppIds);

            const directAppInvolvements = _.chain(appInvolvements)
                .groupBy('kind')
                .map((vs, k) => ({ kind: k, apps: _.map(vs, v => appsById[v.entityReference.id])}))
                .value();

            const indirectAppInvolvements = _.map(indirectlyInvolvedAppIds, id => appsById[id]);

            const summary = {
                direct: directAppInvolvements,
                indirect: indirectAppInvolvements,
                allApps: apps
            };

            state.model.apps = apps;
            state.model.involvements = summary;

            return state.model;

        });
    }


    function loadCostStats(personId) {
        assetCostViewService
            .initialise(personId, 'PERSON', 'CHILDREN', 2015)
            .then(assetCostData => state.model.assetCostData = assetCostData);
    }


    function loadComplexity(appIds) {
        complexityStore
            .findByAppIds(appIds)
            .then(complexity => state.model.complexity = complexity);
    }


    function loadFlows(personId) {
        dataFlowViewService
            .initialise(personId, 'PERSON', 'CHILDREN')
            .then(flows => state.model.dataFlows = flows);
    }

    function loadTechStats(appIds, personId) {
        techStatsService
            .findByAppIds(appIds, personId, 'PERSON', 'CHILDREN')
            .then(stats => state.model.techStats = stats);
    }


    function reset() {
        state.model = { ...initModel };
    }


    function load(employeeId) {
        reset();
        loadPeople(employeeId)
            .then(() => state.model.person.id)
            .then(personId => {
                loadFlows(personId);
                loadCostStats(personId);
            });

        loadApplications(employeeId)
            .then(({ apps }) => {
                const appIds = _.map(apps, 'id');
                loadComplexity(appIds);
                loadTechStats(appIds, state.model.person.id);
            });
    }


    function selectAssetBucket(bucket) {
        assetCostViewService.selectBucket(bucket);
        assetCostViewService.loadDetail()
            .then(data => state.model.assetCostData = data);
    }


    function loadFlowDetail() {
        dataFlowViewService.loadDetail();
    }


    return {
        load,
        state,
        selectAssetBucket,
        loadFlowDetail
    };
}

service.$inject = [
    'PersonStore',
    'InvolvementDataService',
    'AssetCostViewService',
    'ComplexityStore',
    'DataFlowViewService',
    'TechnologyStatisticsService',
    '$q'
];

export default service;

