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


const initModel = {
    managers: [],
    directs: [],
    person: null,
    combinedAppInvolvements: {
        direct: [],
        indirect: [],
        all: []
    },
    apps: [],
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


function buildAppInvolvementSummary(apps = [], involvements = [], involvementKinds = []) {
    const appsById = _.keyBy(apps, 'id');
    const involvementKindsById = _.keyBy(involvementKinds, 'id');

    const directlyInvolvedAppIds = _.chain(involvements).map('entityReference.id').uniq().value();

    const allAppIds = _.map(apps, 'id');
    const indirectlyInvolvedAppIds = _.difference(allAppIds, directlyInvolvedAppIds);

    const directAppInvolvements = _.chain(involvements)
        .groupBy('entityReference.id')
        .map((grp, key) => {
            let app = appsById[key];
            app = _.assign(app, {roles: _.map(grp, g => involvementKindsById[g.kindId].name )});
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
                 entityStatisticStore,
                 involvementStore,
                 involvementKindService,
                 logicalFlowViewService,
                 personStore,
                 physicalFlowLineageStore,
                 sourceDataRatingStore,
                 techStatsService) {

    const state = { model: initModel };

    function reset() {
        state.model = { ...initModel };
    }


    function loadPerson(employeeId) {
        return personStore
            .getByEmployeeId(employeeId)
            .then(person => state.model.person = person);
    }


    function loadRelatedPeople(employeeId) {
        const directsPromise = personStore
            .findDirects(employeeId)
            .then(directs => state.model.directs = directs);

        const managersPromise = personStore
            .findManagers(employeeId)
            .then(managers => state.model.managers = managers);

        return $q.all([
            directsPromise,
            managersPromise]);
    }


    function loadEndUserApps(personId) {
        const endUserAppIdSelector = {
            desiredKind: 'END_USER_APPLICATION',
            entityReference: {
                kind: 'PERSON',
                id: personId
            },
            scope: 'CHILDREN'
        };

        return involvementStore
            .findEndUserAppsByIdSelector(endUserAppIdSelector)
            .then(endUserApps => _.map(
                _.cloneDeep(endUserApps),
                a => _.assign(a, {
                    management: 'End User',
                    platform: a.kind,
                    kind: 'EUC',
                    overallRating: 'Z'
                })))
            .then(endUserApps => {
                state.model.endUserApps = endUserApps;
                return endUserApps;
            });
    }


    function buildInvolvementSummaries(employeeId, combinedApps = [], involvementKinds = []) {
        return involvementStore
            .findByEmployeeId(employeeId)
            .then(involvements => {
                const involvementsByKind = _.groupBy(involvements, 'entityReference.kind');
                const combinedSummary = buildAppInvolvementSummary(combinedApps, _.concat(
                    involvementsByKind['APPLICATION'] || [],
                    involvementsByKind['END_USER_APPLICATION'] || []
                ), involvementKinds);
                state.model.combinedAppInvolvements = combinedSummary
            });
    }


    function loadAllApplications(employeeId, personId) {
        return $q
            .all([
                loadApplications(employeeId, personId),
                loadEndUserApps(personId),
                involvementKindService.loadInvolvementKinds()
            ])
            .then(([apps, endUserApps, involvementKinds]) => ({combinedApps: _.concat(apps, endUserApps), involvementKinds }))
            .then(({combinedApps, involvementKinds}) => {
                buildInvolvementSummaries(employeeId, combinedApps, involvementKinds)
            });
    }


    function loadApplications(employeeId) {

        return involvementStore
            .findAppsForEmployeeId(employeeId)
            .then((xs = []) => {
                const apps = _.map(xs, a => _.assign(a, {management: 'IT'}));
                state.model.apps = apps;
                return apps;
            });
    }


    function loadChangeInitiatives(employeeId) {
        return involvementStore
            .findChangeInitiativesForEmployeeId(employeeId)
            .then(list => state.model.changeInitiatives = list);
    }


    function loadCostStats(personId) {
        return assetCostViewService
            .initialise(toSelector(personId), 2016)
            .then(assetCostData => state.model.assetCostData = assetCostData);
    }


    function loadComplexity(personId) {
        return complexityStore
            .findBySelector(personId, 'PERSON', 'CHILDREN')
            .then(complexity => state.model.complexity = complexity);
    }


    function loadFlows(personId) {
        return logicalFlowViewService
            .initialise(personId, 'PERSON', 'CHILDREN')
            .then(flows => state.model.dataFlows = flows);
    }


    function loadTechStats(personId) {
        return techStatsService
            .findBySelector(personId, 'PERSON', 'CHILDREN')
            .then(stats => state.model.techStats = stats);
    }

    function loadSourceDataRatings()
    {
        return sourceDataRatingStore
            .findAll()
            .then(ratings => state.model.sourceDataRatings = ratings);
    }

    function loadEntityStatistics() {
        return entityStatisticStore
            .findAllActiveDefinitions()
            .then(defns => state.model.entityStatisticDefinitions = defns);
    }


    function loadLineageReports(personId) {
        return physicalFlowLineageStore
            .findLineageReportsBySelector(toSelector(personId))
            .then(lineageReports => state.model.lineageReports = lineageReports);
    }


    // --- MAIN LOADERS

    function loadFirstWave(empId) {
        return loadPerson(empId);
    }


    function loadSecondWave(employeeId) {
        const personId = state.model.person.id;

        // load flows in parallel to avoid delays
        loadFlows(personId);

        return $q
            .all([
                loadRelatedPeople(employeeId),
                loadAllApplications(employeeId, personId),
                loadCostStats(personId)
            ])
            .then(() => personId);
    }


    function loadThirdWave(employeeId) {
        const personId = state.model.person.id;
        return $q
            .all([
                loadTechStats(personId),
                loadComplexity(personId),
                loadChangeInitiatives(employeeId),
                loadLineageReports(personId)
            ]);
    }


    function loadFourthWave() {
        return $q.all([
            loadSourceDataRatings(),
            loadEntityStatistics()
        ]);
    }


    function load(employeeId) {
        reset();

        return loadFirstWave(employeeId)
            .then(() => loadSecondWave(employeeId))
            .then(() => loadThirdWave(employeeId))
            .then(() => loadFourthWave(employeeId));
    }


    // -- INTERACTION ---

    function loadAllCosts() {
        assetCostViewService
            .loadDetail()
            .then(data => state.model.assetCostData = data);
    }


    function loadFlowDetail() {
        return logicalFlowViewService
            .loadDetail()
            .then(flowData => state.model.dataFlows = flowData);
    }


    return {
        load,
        state,
        loadAllCosts,
        loadFlowDetail
    };
}


service.$inject = [
    '$q',
    'AssetCostViewService',
    'ComplexityStore',
    'EntityStatisticStore',
    'InvolvementStore',
    'InvolvementKindService',
    'LogicalFlowViewService',
    'PersonStore',
    'PhysicalFlowLineageStore',
    'SourceDataRatingStore',
    'TechnologyStatisticsService'
];

export default service;

