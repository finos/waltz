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
    appInvolvements: {
        direct: [],
        indirect: [],
        all: []
    },
    endUserAppInvolvements: {
        direct: [],
        indirect: [],
        all: []
    },
    combinedAppInvolvements: {
        direct: [],
        indirect: [],
        all: []
    },
    apps: [],
    appIds: [],
    changeInitiatives: [],
    complexity: [],
    assetCostData: {},
    serverStats: null,
    dataFlows: [],
    visibility: {
        techOverlay: false,
        flowOverlay: false,
        costOverlay: false,
        applicationOverlay: false,
        changeInitiativeOverlay: false
    }
};


function buildAppInvolvementSummary(apps, involvements) {
    const appsById = _.keyBy(apps, 'id');

    const directlyInvolvedAppIds = _.map(involvements, 'entityReference.id');

    const allAppIds = _.map(apps, 'id');
    const indirectlyInvolvedAppIds = _.difference(allAppIds, directlyInvolvedAppIds);

    const directAppInvolvements = _.chain(involvements)
        .map(inv => {
            let app = appsById[inv.entityReference.id];
            app = _.assign(app, {role: inv.kind})
            return app;
        })
        .value();

    const indirectAppInvolvements = _.map(indirectlyInvolvedAppIds, id => appsById[id]);

    const summary = {
        direct: directAppInvolvements,
        indirect: indirectAppInvolvements,
        all: apps
    };
    return summary;
}


function service($q,
                 assetCostViewService,
                 complexityStore,
                 dataFlowViewService,
                 involvementStore,
                 personStore,
                 sourceDataRatingStore,
                 techStatsService) {

    const state = { model: initModel };

    function loadPeople(employeeId) {
        const personPromise = personStore
            .getByEmployeeId(employeeId)
            .then(person => state.model.person = person);

        const directsPromise = personStore
            .findDirects(employeeId)
            .then(directs => state.model.directs = directs);

        const managersPromise = personStore
            .findManagers(employeeId)
            .then(managers => state.model.managers = managers);

        return $q.all([personPromise, directsPromise, managersPromise]);
    }

    function loadApplications(employeeId, personId) {
        const endUserAppIdSelector = {
            desiredKind: 'END_USER_APPLICATION',
            entityReference: {
                kind: 'PERSON',
                id: personId
            },
            scope: 'CHILDREN'
        };

        return $q.all([
            involvementStore.findByEmployeeId(employeeId),
            involvementStore.findAppsForEmployeeId(employeeId),
            involvementStore.findEndUserAppsBydSelector(endUserAppIdSelector)
        ]).then(([involvements, apps, endUserApps]) => {

            const appsSummary = buildAppInvolvementSummary(apps,
                _.filter(involvements, i => i.entityReference.kind === 'APPLICATION'));
            const endUserAppsSummary = buildAppInvolvementSummary(endUserApps,
                _.filter(involvements, i => i.entityReference.kind === 'END_USER_APPLICATION'));

            const appsWithManagement = _.map(apps, a => _.assign(a, {management: 'IT'}));
            const endUserAppsWithManagement = _.map(_.cloneDeep(endUserApps),
                a => _.assign(a, {
                    management: 'End User',
                    platform: a.kind,
                    kind: 'EUC',
                    overallRating: 'Z'
                }));
            const combinedApps = _.concat(appsWithManagement, endUserAppsWithManagement);
            const combinedSummary = buildAppInvolvementSummary(combinedApps, involvements);

            state.model.apps = apps;
            state.model.appInvolvements = appsSummary;
            state.model.endUserAppInvolvements = endUserAppsSummary;
            state.model.combinedAppInvolvements = combinedSummary;

            return state.model;
        });
    }


    function loadChangeInitiatives(employeeId) {
        involvementStore
            .findChangeInitiativesForEmployeeId(employeeId)
            .then(list => state.model.changeInitiatives = list);
    }


    function loadCostStats(personId) {
        assetCostViewService
            .initialise(personId, 'PERSON', 'CHILDREN', 2015)
            .then(assetCostData => state.model.assetCostData = assetCostData);
    }


    function loadComplexity(personId) {
        complexityStore
            .findBySelector(personId, 'PERSON', 'CHILDREN')
            .then(complexity => state.model.complexity = complexity);
    }


    function loadFlows(personId) {
        dataFlowViewService
            .initialise(personId, 'PERSON', 'CHILDREN')
            .then(flows => state.model.dataFlows = flows);
    }


    function loadTechStats(personId) {
        techStatsService
            .findBySelector(personId, 'PERSON', 'CHILDREN')
            .then(stats => state.model.techStats = stats);
    }

    function loadSourceDataRatings()
    {
        sourceDataRatingStore
            .findAll()
            .then(ratings => state.model.sourceDataRatings = ratings);
    }

    function reset() {
        state.model = { ...initModel };
    }


    function load(employeeId) {
        reset();

        loadChangeInitiatives(employeeId);

        const peoplePromise = loadPeople(employeeId)
            .then(() => state.model.person.id);

        const statsPromises = peoplePromise
            .then(personId => {
                loadFlows(personId);
                loadCostStats(personId);
                loadTechStats(personId);
                loadComplexity(personId);
                loadSourceDataRatings();
            });

        const appPromise = peoplePromise
            .then((personId) => loadApplications(employeeId, personId));

        return $q.all([statsPromises, appPromise]);
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
    '$q',
    'AssetCostViewService',
    'ComplexityStore',
    'DataFlowViewService',
    'InvolvementStore',
    'PersonStore',
    'SourceDataRatingStore',
    'TechnologyStatisticsService'
];

export default service;

