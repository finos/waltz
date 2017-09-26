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
import template from "./app-view.html";
import {CORE_API} from "../common/services/core-api-utils";


const initialState = {
    app: {},
    surveyRuns: [],
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
                    serviceBroker,
                    flowDiagramStore,
                    flowDiagramEntityStore,
                    historyStore,
                    measurableStore,
                    measurableCategoryStore,
                    measurableRatingStore,
                    perspectiveRatingStore,
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
        return serviceBroker
            .loadViewData(CORE_API.ApplicationStore.getById, [id])
            .then(r => vm.app = r.data);
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

            serviceBroker
                .loadAppData(CORE_API.PerspectiveDefinitionStore.findAll)
                .then(r => vm.perspectiveDefinitions = r.data),

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


}


controller.$inject = [
    '$q',
    '$state',
    '$stateParams',
    'ServiceBroker',
    'FlowDiagramStore',
    'FlowDiagramEntityStore',
    'HistoryStore',
    'MeasurableStore',
    'MeasurableCategoryStore',
    'MeasurableRatingStore',
    'PerspectiveRatingStore',
    'SurveyInstanceStore',
    'SurveyRunStore'
];


export default  {
    template,
    controller,
    controllerAs: 'ctrl'
};

