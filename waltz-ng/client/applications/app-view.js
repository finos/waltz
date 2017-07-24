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
import {
    loadAuthSources,
    loadDatabases,
    loadDataFlows,
    loadDataTypeUsages,
    loadInvolvements,
    loadLogicalFlowDecorators,
    loadServers,
    loadSoftwareCatalog
} from "./data-load";

import template from "./app-view.html";


const initialState = {
    app: {},
    aliases: [],
    appAuthSources: [],
    complexity: [],
    databases: [],
    specifications: [],
    dataTypes: [],
    dataTypeUsages: [],
    flows: [],
    ouAuthSources: [],
    organisationalUnit: null,
    peopleInvolvements: [],
    servers: [],
    softwareCatalog: [],
    surveyInstances: [],
    surveyRuns: [],
    tags: [],
    visibility: {},
    physicalFlowsProducesCount: 0,
    physicalFlowsConsumesCount: 0,
    physicalFlowsUnusedSpecificationsCount: 0,
    physicalFlowProducesExportFn: () => {},
    physicalFlowConsumesExportFn: () => {},
    physicalFlowUnusedSpecificationsExportFn: () => {}
};


const addToHistory = (historyStore, app) => {
    if (! app) { return; }
    historyStore.put(
        app.name,
        'APPLICATION',
        'main.app.view',
        { id: app.id });
};



function loadFlowDiagrams(appId, $q, flowDiagramStore, flowDiagramEntityStore) {
    const ref = {
        id: appId,
        kind: 'APPLICATION'
    };

    const selector = {
        entityReference: ref,
        scope: 'EXACT'
    };

    const promises = [
        flowDiagramStore.findForSelector(selector),
        flowDiagramEntityStore.findForSelector(selector)
    ];
    return $q
        .all(promises)
        .then(([flowDiagrams, flowDiagramEntities]) => ({ flowDiagrams, flowDiagramEntities }));
}



function controller($q,
                    $state,
                    $stateParams,
                    appViewStore,
                    assetCostStore,
                    aliasStore,
                    authSourcesStore,
                    databaseStore,
                    dataTypeUsageStore,
                    entityStatisticStore,
                    entityTagStore,
                    flowDiagramStore,
                    flowDiagramEntityStore,
                    historyStore,
                    involvedSectionService,
                    involvementStore,
                    logicalFlowDecoratorStore,
                    logicalFlowStore,
                    measurableStore,
                    measurableCategoryStore,
                    measurableRatingStore,
                    orgUnitStore,
                    perspectiveDefinitionStore,
                    perspectiveRatingStore,
                    physicalSpecificationStore,
                    physicalFlowStore,
                    serverInfoStore,
                    softwareCatalogStore,
                    surveyInstanceStore,
                    surveyRunStore)
{

    const id = $stateParams.id;
    const entityReference = { id, kind: 'APPLICATION' };
    const vm = Object.assign(this, initialState);

    const goToAppFn = d => $state.go('main.app.view', { id: d.id });
    vm.flowTweakers = {
        source: {
            onSelect: goToAppFn,
        },
        target: {
            onSelect: goToAppFn,
        }
    };
    vm.entityRef = entityReference;

    vm.saveAliases = (aliases = []) => {
        return aliasStore
            .update(entityReference, aliases)
            .then(() => vm.aliases = aliases);
    };

    vm.saveTags = (tags = []) => {
        return entityTagStore
            .update(entityReference, tags)
            .then(xs => vm.tags = xs);
    };

    vm.onPhysicalFlowsInitialise = (e) => {
        vm.physicalFlowProducesExportFn = e.exportProducesFn;
        vm.physicalFlowConsumesExportFn = e.exportConsumesFn;
        vm.physicalFlowUnusedSpecificationsExportFn = e.exportUnusedSpecificationsFn;
    };

    vm.exportPhysicalFlowProduces = () => {
        vm.physicalFlowProducesExportFn();
    };

    vm.exportPhysicalFlowConsumes = () => {
        vm.physicalFlowConsumesExportFn();
    };

    vm.exportPhysicalFlowUnusedSpecifications = () => {
        vm.physicalFlowUnusedSpecificationsExportFn();
    };


    function loadAll() {
        loadFirstWave()
            .then(() => loadSecondWave())
            .then(() => loadThirdWave())
            .then(() => loadFourthWave())
            .then(() => postLoadActions());
    }


    function loadFirstWave() {
        const promises = [
            appViewStore.getById(id)
                .then(appView => Object.assign(vm, appView)),
        ];

        return $q.all(promises);
    }


    vm.loadFlowDiagrams = () => {
        loadFlowDiagrams(id, $q, flowDiagramStore, flowDiagramEntityStore)
            .then(r => Object.assign(vm, r));
    };

    function loadSecondWave() {
        const promises = [
            measurableCategoryStore
                .findAll()
                .then(cs => vm.measurableCategories = cs),

            measurableRatingStore
                .findByAppSelector({ entityReference, scope: 'EXACT' })
                .then(rs => vm.ratings = rs),

            measurableStore
                .findMeasurablesRelatedToPath(entityReference)
                .then(ms => vm.measurables = ms),
        ];

        return $q.all(promises);
    }


    function loadThirdWave() {
        const promises = [
            loadDataFlows(logicalFlowStore, id, vm),
            loadInvolvements($q, involvementStore, id, vm),
            loadAuthSources(authSourcesStore, orgUnitStore, id, vm.organisationalUnit.id, vm),
            loadServers(serverInfoStore, id, vm),
            loadSoftwareCatalog(softwareCatalogStore, id, vm),
            loadDatabases(databaseStore, id, vm),
            loadDataTypeUsages(dataTypeUsageStore, id, vm),
            loadLogicalFlowDecorators(logicalFlowDecoratorStore, id, vm),

            physicalSpecificationStore
                .findByEntityReference(entityReference)
                .then(xs => vm.physicalSpecifications = xs),

            physicalFlowStore
                .findByEntityReference(entityReference)
                .then(xs => vm.physicalFlows = xs),

            entityStatisticStore
                .findStatsForEntity(entityReference)
                .then(stats => vm.entityStatistics = stats),

            assetCostStore
                .findByAppId(id)
                .then(costs => vm.costs = costs),

            perspectiveDefinitionStore
                .findAll()
                .then(pds => vm.perspectiveDefinitions = pds),

            perspectiveRatingStore
                .findForEntity(entityReference)
                .then(prs => vm.perspectiveRatings = prs),

        ];

        return $q.all(promises);
    }

    function loadFourthWave() {
        const promises = [
            surveyRunStore
                .findByEntityReference(vm.entityRef)
                .then(surveyRuns => vm.surveyRuns = surveyRuns),

            // only get back completed instances
            surveyInstanceStore
                .findByEntityReference(vm.entityRef)
                .then(surveyInstances => vm.surveyInstances = _.filter(surveyInstances, {'status': 'COMPLETED'}))
        ];

        return $q.all(promises);
    }

    function postLoadActions() {
        addToHistory(historyStore, vm.app);
        vm.entityRef = Object.assign({}, vm.entityRef, {name: vm.app.name});
    }

    // load everything in priority order
    loadAll();



    vm.createFlowDiagramCommands = () => {
        const app = Object.assign({}, vm.app, { kind: 'APPLICATION' });
        const title = `${app.name} flows`;
        const annotation = {
            id: +new Date()+'',
            kind: 'ANNOTATION',
            entityReference: app,
            note: `${app.name} data flows`
        };

        const modelCommands = [
            { command: 'ADD_NODE', payload: app },
            { command: 'ADD_ANNOTATION', payload: annotation },
            { command: 'SET_TITLE', payload: title }
        ];

        const moveCommands = [
            { command: 'MOVE', payload: { id: `ANNOTATION/${annotation.id}`, dx: 100, dy: -50 }},
            { command: 'MOVE', payload: { id: `APPLICATION/${app.id}`, dx: 300, dy: 200 }},
        ];

        return _.concat(modelCommands, moveCommands);
    };


    vm.onAddInvolvement = (entityInvolvement) => {

        involvedSectionService.addInvolvement(vm.entityRef, entityInvolvement)
            .then(() => loadInvolvements($q, involvementStore, id, vm));
    };


    vm.onRemoveInvolvement = (entityInvolvement) => {

        involvedSectionService.removeInvolvement(vm.entityRef, entityInvolvement)
            .then(() => loadInvolvements($q, involvementStore, id, vm));
    };
}


controller.$inject = [
    '$q',
    '$state',
    '$stateParams',
    'ApplicationViewStore',
    'AssetCostStore',
    'AliasStore',
    'AuthSourcesStore',
    'DatabaseStore',
    'DataTypeUsageStore',
    'EntityStatisticStore',
    'EntityTagStore',
    'FlowDiagramStore',
    'FlowDiagramEntityStore',
    'HistoryStore',
    'InvolvedSectionService',
    'InvolvementStore',
    'LogicalFlowDecoratorStore',
    'LogicalFlowStore',
    'MeasurableStore',
    'MeasurableCategoryStore',
    'MeasurableRatingStore',
    'OrgUnitStore',
    'PerspectiveDefinitionStore',
    'PerspectiveRatingStore',
    'PhysicalSpecificationStore',
    'PhysicalFlowStore',
    'ServerInfoStore',
    'SoftwareCatalogStore',
    'SurveyInstanceStore',
    'SurveyRunStore'
];


export default  {
    template,
    controller,
    controllerAs: 'ctrl'
};

