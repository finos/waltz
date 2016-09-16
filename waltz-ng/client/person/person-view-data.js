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
    entityStatisticDefinitions: [],
    visibility: {
        techOverlay: false,
        flowOverlay: false,
        costOverlay: false,
        applicationOverlay: false,
        changeInitiativeOverlay: false
    }
};


function toSelector(personId, scope='CHILDREN') {
    return { entityReference: { kind: 'PERSON', id: personId }, scope };
}


function buildAppInvolvementSummary(apps = [], involvements = []) {
    const appsById = _.keyBy(apps, 'id');

    const directlyInvolvedAppIds = _.chain(involvements).map('entityReference.id').uniq().value();



    const allAppIds = _.map(apps, 'id');
    const indirectlyInvolvedAppIds = _.difference(allAppIds, directlyInvolvedAppIds);

    const directAppInvolvements = _.chain(involvements)
        .groupBy('entityReference.id')
        .map((grp, key) => {
            let app = appsById[key];
            app = _.assign(app, {roles: _.map(grp, g => g.kind)});
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
                 entityStatisticStore,
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

            const involvementsByKind = _.groupBy(involvements, 'entityReference.kind');

            const appsSummary = buildAppInvolvementSummary(apps, involvementsByKind['APPLICATION']);
            const endUserAppsSummary = buildAppInvolvementSummary(endUserApps, involvementsByKind['END_USER_APPLICATION']);

            const appsWithManagement = _.map(apps, a => _.assign(a, {management: 'IT'}));
            const endUserAppsWithManagement = _.map(_.cloneDeep(endUserApps),
                a => _.assign(a, {
                    management: 'End User',
                    platform: a.kind,
                    kind: 'EUC',
                    overallRating: 'Z'
                }));
            const combinedApps = _.concat(appsWithManagement, endUserAppsWithManagement);

            const combinedSummary = buildAppInvolvementSummary(combinedApps, _.concat(
                involvementsByKind['APPLICATION'] || [],
                involvementsByKind['END_USER_APPLICATION'] || []
            ));

            state.model.apps = appsWithManagement;
            state.model.endUserApps = endUserAppsWithManagement
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
            .initialise(toSelector(personId), 2016)
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

    function loadEntityStatistics() {
        return entityStatisticStore
            .findAllActiveDefinitions()
            .then(defns => state.model.entityStatisticDefinitions = defns);
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
                loadEntityStatistics();
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
        return dataFlowViewService.loadDetail();
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
    'EntityStatisticStore',
    'InvolvementStore',
    'PersonStore',
    'SourceDataRatingStore',
    'TechnologyStatisticsService'
];

export default service;

