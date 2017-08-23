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
import {CORE_API} from "../common/services/core-api-utils";


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
    complexity: [],
    assetCostData: {},
    serverStats: null,
    dataFlows: [],
    visibility: {
        techOverlay: false,
        flowOverlay: false,
        costOverlay: false,
        applicationOverlay: false,
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
                 serviceBroker,
                 complexityStore,
                 logicalFlowViewService) {

    const state = { model: initModel };

    function reset() {
        state.model = { ...initModel };
    }


    function loadPerson(employeeId) {
        return serviceBroker
            .loadViewData(CORE_API.PersonStore.getByEmployeeId, [ employeeId ])
            .then(r => state.model.person = r.data);
    }


    function loadRelatedPeople(employeeId) {
        const directsPromise = serviceBroker
            .loadViewData(CORE_API.PersonStore.findDirects, [ employeeId ])
            .then(r => state.model.directs = r.data);

        const managersPromise = serviceBroker
            .loadViewData(CORE_API.PersonStore.findManagers, [ employeeId ])
            .then(r => state.model.managers = r.data);

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

        return serviceBroker
            .loadViewData(CORE_API.InvolvementStore.findEndUserAppsByIdSelector, [ endUserAppIdSelector ])
            .then(r => _.map(
                _.cloneDeep(r.data),
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
        return serviceBroker
            .loadViewData(CORE_API.InvolvementStore.findByEmployeeId, [ employeeId ])
            .then(r => r.data)
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
                serviceBroker.loadAppData(CORE_API.InvolvementKindStore.findAll).then(r => r.data)
            ])
            .then(([apps, endUserApps, involvementKinds]) => ({combinedApps: _.concat(apps, endUserApps), involvementKinds }))
            .then(({combinedApps, involvementKinds}) => {
                buildInvolvementSummaries(employeeId, combinedApps, involvementKinds);
            });
    }


    function loadApplications(employeeId) {

        return serviceBroker
            .loadViewData(CORE_API.InvolvementStore.findAppsForEmployeeId, [ employeeId ])
            .then(r => r.data)
            .then((xs = []) => {
                const apps = _.map(xs, a => _.assign(a, {management: 'IT'}));
                state.model.apps = apps;
                return apps;
            });
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
        const selector = toSelector(personId);

        return serviceBroker
            .loadViewData(CORE_API.TechnologyStatisticsService.findBySelector, [selector])
            .then(r => state.model.techStats = r.data);
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
                loadAllApplications(employeeId, personId)
            ])
            .then(() => personId);
    }


    function loadThirdWave(employeeId) {
        const personId = state.model.person.id;
        return $q
            .all([
                loadTechStats(personId),
                loadComplexity(personId),
            ]);
    }


    function load(employeeId) {
        reset();

        return loadFirstWave(employeeId)
            .then(() => loadSecondWave(employeeId))
            .then(() => loadThirdWave(employeeId));
    }


    // -- INTERACTION ---


    function loadFlowDetail() {
        return logicalFlowViewService
            .loadDetail()
            .then(flowData => state.model.dataFlows = flowData);
    }


    return {
        load,
        state,
        loadFlowDetail
    };
}


service.$inject = [
    '$q',
    'ServiceBroker',
    'ComplexityStore',
    'LogicalFlowViewService'
];

export default service;

